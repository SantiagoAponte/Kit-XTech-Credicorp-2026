package com.andes.example.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.InputStream;
import java.util.Set;

/**
 * Valida respuestas reales contra example.openapi.yaml. OpenAPI 3.1 usa JSON Schema 2020-12 para sus esquemas,
 * así que se validan con networknt (dialecto 2020-12) resolviendo los $ref contra el documento completo.
 * Se comprueba: operación/estado declarados, media type declarado y esquema del cuerpo.
 */
final class OpenApiContractValidator {

    private static final ObjectMapper JSON = new ObjectMapper();
    private final JsonNode root;
    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private final SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
            .formatAssertionsEnabled(true).build();

    OpenApiContractValidator(String classpathResource) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(classpathResource)) {
            this.root = new ObjectMapper(new YAMLFactory()).readTree(in);
        }
    }

    /** @return lista de errores de contrato (vacía = conforme). */
    Set<ValidationMessage> validateResponse(String method, String pathTemplate, int status, String contentType,
                                            String body) throws Exception {
        JsonNode operation = root.at("/paths").path(pathTemplate).path(method.toLowerCase());
        if (operation.isMissingNode()) {
            throw new AssertionError("Operación no declarada en el contrato: " + method + " " + pathTemplate);
        }
        JsonNode response = resolve(operation.path("responses").path(String.valueOf(status)));
        if (response.isMissingNode()) {
            throw new AssertionError("Estado " + status + " no declarado para " + method + " " + pathTemplate);
        }
        String mediaType = contentType == null ? "" : contentType.split(";")[0].trim();
        JsonNode media = response.path("content").path(mediaType);
        if (media.isMissingNode()) {
            throw new AssertionError("Media type '" + mediaType + "' no declarado para " + status + " de "
                    + method + " " + pathTemplate);
        }
        return validate(media.path("schema"), JSON.readTree(body));
    }

    Set<ValidationMessage> validateSchema(String schemaName, JsonNode instance) {
        ObjectNode ref = JSON.createObjectNode().put("$ref", "#/components/schemas/" + schemaName);
        return validate(ref, instance);
    }

    private Set<ValidationMessage> validate(JsonNode schema, JsonNode instance) {
        ObjectNode wrapper = JSON.createObjectNode();
        wrapper.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        wrapper.setAll((ObjectNode) schema.deepCopy());
        wrapper.set("components", root.get("components"));
        JsonSchema compiled = factory.getSchema(wrapper, config);
        return compiled.validate(instance);
    }

    private JsonNode resolve(JsonNode node) {
        if (node.has("$ref")) {
            String ref = node.get("$ref").asText();
            return root.at(ref.substring(1));
        }
        return node;
    }
}
