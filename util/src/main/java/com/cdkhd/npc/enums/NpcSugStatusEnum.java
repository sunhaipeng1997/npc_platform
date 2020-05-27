package com.cdkhd.npc.enums;

public enum NpcSugStatusEnum {
    //小程序代表查看建议，分状态（全部、已审核、未审核）
    All((byte)0,"全部"),
    NOT_SUBMIT((byte)1,"草稿"),  //对代表来说：草稿；对审核人员来说：待审核
    TO_BE_AUDITED((byte)2,"已提交"), //对代表来说：已提交；对审核人员来说：审核通过
    AUDIT_FAILURE((byte)3,"审核失败"), //对审核人员来说：审核失败
    DONE((byte)4,"已办完"),
    COMPLETED((byte)5,"已办结");

    private Byte value;
    private String name;

    NpcSugStatusEnum(Byte value, String name) {
        this.value = value;
        this.name = name;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Byte getValue(String name) {
        NpcSugStatusEnum[] levels = values();
        for (NpcSugStatusEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        NpcSugStatusEnum[] levels = values();
        for (NpcSugStatusEnum level : levels) {
            if (level.getValue().equals(value)) {
                return level.getName();
            }
        }
        return null;
    }
}
