package de.igslandstuhl.database.api;

public enum GraduationLevel {
    LEVEL0 (0, "Neustarter"),
    LEVEL1 (1, "Starter"),
    LEVEL2 (2, "Durchstarter"),
    LEVEL3 (3, "Lernprofi");

    private final int level;
    private final String germanTranslation;


    private GraduationLevel(int level, String germanTranslation) {
        this.level = level;
        this.germanTranslation = germanTranslation;
    }

    public int getLevel() {
        return level;
    }

    public String getGermanTranslation() {
        return germanTranslation;
    }

    @Override
    public String toString() {
        return germanTranslation;
    }

    public static GraduationLevel of(int level) {
        switch (level) {
            case 0:
                return LEVEL0;
            case 1:
                return LEVEL1;
            case 2:
                return LEVEL2;
            case 3:
                return LEVEL3;
            default:
                throw new IllegalArgumentException("No such graduation level: " + level);
        }
    }

    public static GraduationLevel initialValue() {
        return LEVEL1;
    }
}
