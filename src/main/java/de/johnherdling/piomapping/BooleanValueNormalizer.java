package de.johnherdling.piomapping;

public class BooleanValueNormalizer {

    public Boolean normalize(String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim().toLowerCase();

        if (normalized.equals("true")
                || normalized.equals("yes")
                || normalized.equals("present")) {
            return true;
        }

        if (normalized.equals("false")
                || normalized.equals("no")
                || normalized.equals("absent")) {
            return false;
        }

        return null;
    }
}