package de.igslandstuhl.database.api;

public enum Level {
    LEVEL1, LEVEL2, LEVEL3;

    public static Level get(int number) {
        switch (number) {
            case 1:
                return LEVEL1;
            case 2:
                return LEVEL2;
            case 3:
                return LEVEL3;
            default:
                throw new IllegalArgumentException(number + " out of range [1,3]");
        }
    }

    public double getRatio() {
        switch (this) {
            case LEVEL1:
                return 0.45;
            case LEVEL2:
                return 0.3;
            case LEVEL3:
                return 0.25;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case LEVEL1:
                return "1";
            case LEVEL2:
                return "2";
            case LEVEL3:
                return "3";
            default:
                throw new IllegalStateException();
        }
    }
}
