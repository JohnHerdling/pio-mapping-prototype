package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonSourceReader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode read(String filePath) throws IOException {
        return objectMapper.readTree(new File(filePath));
    }
}