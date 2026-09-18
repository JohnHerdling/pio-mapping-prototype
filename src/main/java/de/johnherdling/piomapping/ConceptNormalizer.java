package de.johnherdling.piomapping;

public class ConceptNormalizer {

    public String normalize(String text) {

        if (text == null) {
            return "";
        }

        String normalized =
                text.toLowerCase()
                        .replace("-", " ")
                        .replace("_", " ");

        normalized = normalized.replace("problems", "diagnosis");
        normalized = normalized.replace("problem", "diagnosis");

        normalized = normalized.replace("intolerances", "allergy");
        normalized = normalized.replace("intolerance", "allergy");

        return normalized;
    }
}