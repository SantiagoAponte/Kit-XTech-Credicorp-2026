package com.andes.example.adapter.in.web;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

/** Acepta solo tokens string JSON: un número (coma flotante) en un campo de dinero es 400, no se coacciona. */
public class StrictStringDeserializer extends StdDeserializer<String> {

    public StrictStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() != JsonToken.VALUE_STRING) {
            return (String) ctxt.handleUnexpectedToken(String.class, p);
        }
        return p.getText();
    }
}
