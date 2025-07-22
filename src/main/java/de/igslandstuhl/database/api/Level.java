package de.igslandstuhl.database.api;

/**
 * Represents different levels of difficulty for tasks.
 * Each level has a specific ratio that indicates the proportion of the progress
 * that can be achieved at that level.
 */
public enum Level {
    LEVEL1, LEVEL2, LEVEL3, SPECIAL;

    /**
     * Returns the Level corresponding to the given number.
     * @param number the level number (1-3)
     * @return the corresponding Level
     * @throws IllegalArgumentException if the number is out of range
     *                                  (not between 1 and 3 inclusive)
     */
    public static Level get(int number) {
        switch (number) {
            case 1:
                return LEVEL1;
            case 2:
                return LEVEL2;
            case 3:
                return LEVEL3;
            case -1:
                return SPECIAL;
            default:
                throw new IllegalArgumentException(number + " out of range [1,3]");
        }
    }

    /**
     * Returns the ratio associated with this level.
     * The ratio indicates the proportion of progress that can be achieved at this level.
     * @return the ratio as a double
     */
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
    /**
     * Returns a string representation of the level.
     * This is useful for displaying the level in a user-friendly format.
     */
    @Override
    public String toString() {
        switch (this) {
            case LEVEL1:
                return "1";
            case LEVEL2:
                return "2";
            case LEVEL3:
                return "3";
            case SPECIAL:
                return "Special";
            default:
                throw new IllegalStateException();
        }
    }
}
