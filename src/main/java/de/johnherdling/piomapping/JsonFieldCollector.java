package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

public class JsonFieldCollector {

    public void collect(JsonNode node, String path) {

        if (node.isObject()) {

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();

                String fieldName = field.getKey();
                JsonNode childNode = field.getValue();

                String childPath;

                if (path.isEmpty()) {
                    childPath = fieldName;
                } else {
                    childPath = path + "." + fieldName;
                }

                collect(childNode, childPath);
            }

    } else if (node.isArray()) {
        
    } else {

    }
}
}