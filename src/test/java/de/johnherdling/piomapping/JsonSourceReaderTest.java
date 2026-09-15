package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSourceReaderTest {

@Test
void readsWeightFromQ1() throws IOException {

    JsonSourceReader reader = new JsonSourceReader();

    JsonNode root = reader.read("data/q1.json");

    int weight = root.get("weight").asInt();

    assertEquals(82, weight);
}
}