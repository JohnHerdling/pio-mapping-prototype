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
                || normalized.equals("detected")
                || normalized.equals("1")) {
            return true;
        }

        if (normalized.equals("false")
                || normalized.equals("no")
                || normalized.equals("absent")
                || normalized.equals("not detected")
                || normalized.equals("0")) {
            return false;
        }

        return null;
    }
}