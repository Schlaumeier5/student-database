package de.igslandstuhl.database.api;

public enum SubjectRequest {
    HELP ("hilfe"),
    PARTNER ("partner"),
    EXPERIMENT ("betreuung"),
    EXAM ("gelingensnachweis");

    private String germanTranslation;

    SubjectRequest(String germanTranslation) {
        this.germanTranslation = germanTranslation;
    }

    public String getGermanTranslation() {
        return germanTranslation;
    }
    public static SubjectRequest fromGermanTranslation(String translation) {
        for (SubjectRequest request : SubjectRequest.values()) {
            if (request.germanTranslation.equalsIgnoreCase(translation)) {
                return request;
            }
        }
        throw new IllegalArgumentException("No matching SubjectRequest for translation: " + translation);
    }
}
