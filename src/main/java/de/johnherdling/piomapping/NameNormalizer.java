package de.johnherdling.piomapping;

public class NameNormalizer {

    public String normalize(String text) {

        String normalized = text.replaceAll(
                "([a-z])([A-Z])",
                "$1 $2"
        );

        normalized = normalized.replace("-", " ");
        normalized = normalized.replace("_", " ");

        normalized = normalized.toLowerCase();

        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }
}