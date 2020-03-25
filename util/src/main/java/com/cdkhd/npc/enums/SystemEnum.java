package com.cdkhd.npc.enums;

public enum SystemEnum {
    //小程序菜单类型
    MEMBER_HOUSE("代表之家平台","home","memberHouse",null),//name   type   url   parent
    SUGGESTION("建议办理系统","handle",null,null),
    PERFORMANCE("履职登记评价系统","duty",null,null),
    METTING("会议签到系统"," register",null,null),
    VOTE("投票系统"," vote",null,null);

    private String name;
    private String svg;
    private String url;
    private String description;

    SystemEnum(String name, String svg, String url, String description) {
        this.name = name;
        this.svg = svg;
        this.url = url;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getSvg() {
        return svg;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }
}
