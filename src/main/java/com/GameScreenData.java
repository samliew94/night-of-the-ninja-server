package com;

public enum GameScreenData {

    LOBBY("lobby"),
    VIEW_HOUSE("view-house"),
    SELECT_ONE_USER("select-one-user"),
    WAITING_FOR("waiting-for"),
    MESSAGES("messages"),
    DRAFT_ONE("draft-one"),
    DRAFT_ONE_COMPLETE("draft-one-complete"),
    DRAFT_TWO("draft-two"),
    DRAFT_TWO_COMPLETE("draft-two-complete"),
    SPIES_PLAY("spies-play"),
    SPIES_SELECT_TARGET("spies-select-target"),
    SPIES_SELECTED_TARGET("spies-selected-target"),
    MYSTICS_PLAY("mystics-play"),
    MYSTICS_SELECT_TARGET("mystics-select-target"),
    MYSTICS_SELECTED_TARGET("mystics-selected-target"),
    TRICKSTERS_PLAY("tricksters-play"),
    TRICKSTERS_SHAPE_SELECT_TARGETS("tricksters-shape-select-target"),
    TRICKSTERS_SHAPE_SELECTED_TARGETS("tricksters-shape-selected-target"),
    TRICKSTERS_TROUBLE_SELECT_TARGET("tricksters-trouble-select-target"),
    TRICKSTERS_TROUBLE_SELECTED_TARGET("tricksters-trouble-selected-target"),

    TRICKSTERS_GRAVE_SELECT_TARGET("tricksters-grave-select-target"),
    TRICKSTERS_GRAVE_SELECTED_TARGETS("tricksters-grave-selected-target"),
    TRICKSTERS_SPIRIT_SELECT_TARGET("tricksters-spirit-select-target"),
    TRICKSTER_SPIRIT_SELECT_HOUSE_OR_TOKEN("trickster-spirit-select-house-or-token"),
    TRICKSTERS_SPIRIT_VIEW_HOUSE("tricksters-spirit-view-house"),
    TRICKSTERS_SPIRIT_VIEW_TOKENS("tricksters-spirit-view-tokens"),
    TRICKSTERS_SPIRIT_SELECT_TOKEN("tricksters-spirit-select-token"),
    TRICKSTERS_SPIRIT_SWAPPED_TOKEN("tricksters-spirit-swapped-token"),

    TRICKSTERS_SPIRIT_SELECTED_TARGETS("tricksters-spirit-selected-target"),
    TRICKSTERS_THIEF_SELECT_TARGET("tricksters-thief-select-target"),
    TRICKSTERS_THIEF_REVEAL_HOUSE("tricksters-thief-reveal-house"),
    TRICKSTERS_THIEF_SELECT_TOKEN("tricksters-thief-select-token"),
    TRICKSTERS_THIEF_STOLE_TOKEN("tricksters-thief-stole-token"),
    TRICKSTERS_JUDGE_SELECT_PLAYER_TO_EXECUTE("tricksters-judge-select-player-execute"),
    TRICKSTERS_JUDGE_VIEW_OWNER_HOUSE("tricksters-judge-view-owner-house"),
    TRICKSTERS_JUDGE_EXECUTED("tricksters-judge-executed"),
    BLIND_PLAY("blind-play"),
    SHINOBI_PLAY("shinobi-play"),
    MIRROR_MONKED("mirror-monked"),
    YES_NO("yes-no"),
    GAME_OVER("gg"), REVEAL("reveal");


    private final String value;

    private GameScreenData(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}





