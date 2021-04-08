package com.cdkhd.npc.enums;

public enum UrgeScoreEnum {
    NPC_MEMBER("代表", 1,(byte)1),
    NPC_MEMBER_AUDITOR("工委", 2,(byte)2),
    GOVERNMENT("政府", 4,(byte)3);

    private String name;
    private Integer score;
    private Byte type;

    UrgeScoreEnum(String name, Integer score, Byte type) {
        this.name = name;
        this.score = score;
        this.type = type;
    }

    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return this.name;
    }

    public Integer getScore() {
        return this.score;
    }

    public Byte getType() {
        return type;
    }
}
