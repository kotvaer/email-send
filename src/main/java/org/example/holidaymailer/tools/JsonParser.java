package org.example.holidaymailer.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonParser {

    private final ObjectMapper objectMapper;

    public JsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getMessageFromJson(String jsonString) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonString);
        return rootNode.path("content").asText();
    }
}