package com;

public enum GameProgressData {

    LOBBY("lobby"),
    VIEWHOUSE("viewhouse"),
    DRAFT("draft"),
    SPY_MYSTIC("spy-mystic"),
    TRICKSTER("trickster"),

    BLIND("blind"),
    SHINOBI("shinobi"),
    REVEAL("reveal"),
    AWARD_TOKEN("awardtoken");
    private final String value;

    private GameProgressData(String value) {
        this.value = value;
    }

    public static GameProgressData findEnumByValue(String value) {
        for (GameProgressData enumValue : GameProgressData.values()) {
            if (enumValue.toString().equals(value)) {
                return enumValue;
            }
        }
        return null; // If no matching enum value is found
    }

    @Override
    public String toString() {
        return value;
    }
}