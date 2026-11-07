package com.andes.example.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.andes.example.infrastructure.Application;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Prueba de contrato: aplicación completa (H2 + Flyway) y respuestas reales validadas contra
 * contract/example.openapi.yaml (copia del contrato del kit). Validador: networknt JSON Schema 2020-12.
 */
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static OpenApiContractValidator contract;

    @Autowired MockMvc mvc;

    @BeforeAll
    static void load() throws Exception {
        contract = new OpenApiContractValidator("/contract/example.openapi.yaml");
    }

    private static String body(String message, String amount) {
        return "{\"message\":\"" + message + "\",\"amount\":{\"amount\":\"" + amount + "\",\"currency\":\"PEN\"}}";
    }

    private MvcResult create(String key, String body) throws Exception {
        var req = post("/api/v1/pings").contentType("application/json").content(body);
        if (key != null) {
            req = req.header("Idempotency-Key", key);
        }
        return mvc.perform(req).andReturn();
    }

    private void assertConforms(MvcResult r, String method, String path) throws Exception {
        Set<ValidationMessage> errors = contract.validateResponse(method, path, r.getResponse().getStatus(),
                r.getResponse().getContentType(), r.getResponse().getContentAsString());
        assertThat(errors).as("Respuesta %s %s -> %d: %s", method, path, r.getResponse().getStatus(),
                r.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    void validatorIsReal_rejectsNonConformingPayloads() throws Exception {
        assertThat(contract.validateSchema("Money", JSON.readTree("{\"amount\":1000.0,\"currency\":\"PEN\"}")))
                .isNotEmpty();
        assertThat(contract.validateSchema("Money", JSON.readTree("{\"amount\":\"10.001\",\"currency\":\"PEN\"}")))
                .isNotEmpty();
        assertThat(contract.validateSchema("Money", JSON.readTree("{\"amount\":\"10.00\",\"currency\":\"XXX\"}")))
                .isNotEmpty();
        assertThat(contract.validateSchema("Problem", JSON.readTree("{\"title\":\"x\"}"))).isNotEmpty();
        assertThat(contract.validateSchema("Money", JSON.readTree("{\"amount\":\"10.00\",\"currency\":\"PEN\"}")))
                .isEmpty();
    }

    @Test
    void createdAndReplayConformToContract() throws Exception {
        String key = UUID.randomUUID().toString();
        MvcResult first = create(key, body("hola", "1000.00"));
        MvcResult second = create(key, body("distinto", "5.00"));

        assertThat(first.getResponse().getStatus()).isEqualTo(201);
        assertThat(second.getResponse().getStatus()).isEqualTo(200);
        assertConforms(first, "POST", "/api/v1/pings");
        assertConforms(second, "POST", "/api/v1/pings");
        JsonNode a = JSON.readTree(first.getResponse().getContentAsString());
        JsonNode b = JSON.readTree(second.getResponse().getContentAsString());
        assertThat(b).isEqualTo(a);
        assertThat(a.at("/fee/amount").asText()).isEqualTo("5.00");
    }

    @Test
    void errorResponsesConformToContractAndCatalog() throws Exception {
        record Case(MvcResult result, int status, String code) { }
        Case[] cases = {
                new Case(create(UUID.randomUUID().toString(), "{\"message\":\"x\"}"), 400, "EX-0001"),
                new Case(create(null, body("hola", "1.00")), 400, "EX-0005"),
                new Case(create(UUID.randomUUID().toString(), body("x".repeat(141), "1.00")), 422, "EX-0002"),
                new Case(create(UUID.randomUUID().toString(), body("hola", "0.00")), 422, "EX-0003"),
        };
        for (Case c : cases) {
            assertThat(c.result().getResponse().getStatus()).isEqualTo(c.status());
            assertThat(c.result().getResponse().getContentType()).startsWith("application/problem+json");
            assertConforms(c.result(), "POST", "/api/v1/pings");
            JsonNode problem = JSON.readTree(c.result().getResponse().getContentAsString());
            assertThat(problem.get("code").asText()).isEqualTo(c.code());
            assertThat(problem.get("traceId").asText()).isNotBlank();
        }
    }

    @Test
    void getAndNotFoundAndListConformToContract() throws Exception {
        MvcResult created = create(UUID.randomUUID().toString(), body("hola", "250.50"));
        String id = JSON.readTree(created.getResponse().getContentAsString()).get("id").asText();

        MvcResult one = mvc.perform(get("/api/v1/pings/" + id)).andReturn();
        assertThat(one.getResponse().getStatus()).isEqualTo(200);
        assertConforms(one, "GET", "/api/v1/pings/{id}");

        MvcResult missing = mvc.perform(get("/api/v1/pings/no-existe")).andReturn();
        assertThat(missing.getResponse().getStatus()).isEqualTo(404);
        assertConforms(missing, "GET", "/api/v1/pings/{id}");
        assertThat(JSON.readTree(missing.getResponse().getContentAsString()).get("code").asText())
                .isEqualTo("EX-0004");

        MvcResult list = mvc.perform(get("/api/v1/pings?page=0&size=5")).andReturn();
        assertThat(list.getResponse().getStatus()).isEqualTo(200);
        assertConforms(list, "GET", "/api/v1/pings");
        assertThat(JSON.readTree(list.getResponse().getContentAsString()).get("size").asInt()).isEqualTo(5);
    }
}
