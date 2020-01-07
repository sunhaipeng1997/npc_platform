package com.cdkhd.npc.enums;

public enum DirectionEnum {
    UP("上移"),
    DOWN("下移");

    private String name;

    DirectionEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
