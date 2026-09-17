package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class MappingConfigReader {

    private final ObjectMapper objectMapper =
            new ObjectMapper(new YAMLFactory());

    public MappingConfig read(String filePath) throws IOException {

        return objectMapper.readValue(
                new File(filePath),
                MappingConfig.class
        );
    }
}