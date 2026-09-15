package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JsonSourceReaderTest {

@Test
void readsQ1() throws IOException {

    JsonSourceReader reader = new JsonSourceReader();

    JsonNode root = reader.read("data/q1.json");

    assertNotNull(root);
}
@Test
void readsQ2() throws IOException {

    JsonSourceReader reader = new JsonSourceReader();

    JsonNode root = reader.read("data/q2.json");

    assertNotNull(root);
}
@Test
void readsQ3() throws IOException {

    JsonSourceReader reader = new JsonSourceReader();

    JsonNode root = reader.read("data/q3.json");

    assertNotNull(root);
}
}