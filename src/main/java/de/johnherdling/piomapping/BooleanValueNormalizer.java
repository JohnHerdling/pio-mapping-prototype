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
                || normalized.equals("present")
                || normalized.equals("detected")) {
            return true;
        }

        if (normalized.equals("false")
                || normalized.equals("no")
                || normalized.equals("absent")
                || normalized.equals("not detected")) {
            return false;
        }

        try {

            double numericValue =
                    Double.parseDouble(normalized);

            return numericValue > 0;

        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}