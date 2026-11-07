package com.andes.example.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.andes.example.adapter.in.web.PingController;
import com.andes.example.application.dto.CreatePingResult;
import com.andes.example.application.dto.MoneyView;
import com.andes.example.application.dto.PingPageView;
import com.andes.example.application.dto.PingView;
import com.andes.example.application.port.in.CreatePing;
import com.andes.example.application.port.in.GetPing;
import com.andes.example.application.port.in.ListPings;
import com.andes.example.domain.exception.BusinessRuleViolation;
import com.andes.example.domain.exception.NotFound;
import com.andes.example.infrastructure.Application;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PingController.class)
@ContextConfiguration(classes = Application.class)
class PingControllerTest {

    private static final String BODY = "{\"message\":\"hola\",\"amount\":{\"amount\":\"1000.00\",\"currency\":\"PEN\"}}";

    @Autowired MockMvc mvc;
    @MockitoBean CreatePing createPing;
    @MockitoBean GetPing getPing;
    @MockitoBean ListPings listPings;

    private PingView view() {
        return new PingView("p1", "hola", new MoneyView(new BigDecimal("1000.00"), "PEN"),
                new MoneyView(new BigDecimal("5.00"), "PEN"), Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void createReturns201WithMoneyAsString() throws Exception {
        when(createPing.execute(any())).thenReturn(new CreatePingResult(view(), true));

        mvc.perform(post("/api/v1/pings").header("Idempotency-Key", "k1")
                        .contentType("application/json").content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.amount.amount").value("1000.00"))
                .andExpect(jsonPath("$.fee.amount").value("5.00"))
                .andExpect(jsonPath("$.receivedAt").value("2025-01-01T00:00:00Z"));
    }

    @Test
    void replayReturns200() throws Exception {
        when(createPing.execute(any())).thenReturn(new CreatePingResult(view(), false));

        mvc.perform(post("/api/v1/pings").header("Idempotency-Key", "k1")
                        .contentType("application/json").content(BODY))
                .andExpect(status().isOk());
    }

    @Test
    void echoesIncomingCorrelationId() throws Exception {
        when(getPing.execute("p1")).thenReturn(view());

        mvc.perform(get("/api/v1/pings/p1").header("X-Correlation-Id", "abc-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "abc-123"));
    }

    @Test
    void missingIdempotencyKeyIsExr0005() throws Exception {
        mvc.perform(post("/api/v1/pings").contentType("application/json").content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("EX-0005"));
    }

    @Test
    void invalidBodiesAreExr0001() throws Exception {
        String[] bodies = {
                "{}", "not json",
                "{\"message\":\"hola\"}",
                "{\"message\":\"hola\",\"amount\":{\"amount\":1000.0,\"currency\":\"PEN\"}}",
                "{\"message\":\"hola\",\"amount\":{\"amount\":\"abc\",\"currency\":\"PEN\"}}",
                "{\"message\":\"hola\",\"amount\":{\"amount\":\"1.00\",\"currency\":\"XXX\"}}",
                "{\"message\":\"\",\"amount\":{\"amount\":\"1.00\",\"currency\":\"PEN\"}}"
        };
        for (String body : bodies) {
            mvc.perform(post("/api/v1/pings").header("Idempotency-Key", "k1")
                            .contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                    .andExpect(jsonPath("$.code").value("EX-0001"))
                    .andExpect(jsonPath("$.traceId").exists());
        }
    }

    @Test
    void businessRuleIs422() throws Exception {
        when(createPing.execute(any())).thenThrow(new BusinessRuleViolation("EX-0002", "largo"));

        mvc.perform(post("/api/v1/pings").header("Idempotency-Key", "k1")
                        .contentType("application/json").content(BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("EX-0002"))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void notFoundIs404() throws Exception {
        when(getPing.execute("x")).thenThrow(new NotFound("EX-0004", "no existe"));

        mvc.perform(get("/api/v1/pings/x"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("EX-0004"));
    }

    @Test
    void listValidatesPagingAndShapesResponse() throws Exception {
        when(listPings.execute(0, 20)).thenReturn(new PingPageView(List.of(view()), 0, 20, 1));

        mvc.perform(get("/api/v1/pings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("p1"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/pings?size=101")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EX-0001"));
        mvc.perform(get("/api/v1/pings?page=abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EX-0001"));
    }
}
