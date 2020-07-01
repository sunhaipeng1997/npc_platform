package com.cdkhd.npc.enums;

public enum SystemEnum {
    //小程序菜单类型
    BASIC_INFO("基本信息管理系统","home","basicInfo",null, "sub-systems/smart-platform.png", "../sub-systems/npc-home/npc-home"),//name   type   url   parent
    MEMBER_HOUSE("代表之家平台","home","memberHouse",null, "sub-systems/smart-platform.png", "../sub-systems/npc-home/npc-home"),//name   type   url   parent
    SUGGESTION("建议办理系统","handle","suggestionDeal",null, "sub-systems/suggestion-handle.png", "../sub-systems/suggestion-handling-sys/suggestion-handling-sys"),
/*
    SUGGESTION("建议办理系统","handle","suggestion_deal",null, "sub-systems/suggestion-handle.png", "../sub-systems/suggestion-handling-sys/suggestion-handling-sys"),
*/
    PERFORMANCE("履职登记评价系统","duty",null,null, "sub-systems/duty-rate.png", "../sub-systems/performance-evaluation-sys/performance-evaluation-sys"),
    MEETING("会议签到系统","register",null,null, "sub-systems/vote.png", "../sub-systems/voting-sys/voting-sys"),
    VOTE("投票系统","vote",null,null, "sub-systems/check-in.png", "../sub-systems/conference-signing-up-sys/conference-signing-up-sys");

    private String name;//系统名称
    private String svg;//后台系统图标
    private String url;//后台路由地址
    private String description;
    private String imgUrl;//小程序系统图标
    private String pagePath;//小程序跳转路径

    SystemEnum(String name, String svg, String url, String description, String imgUrl, String pagePath) {
        this.name = name;
        this.svg = svg;
        this.url = url;
        this.description = description;
        this.imgUrl = imgUrl;
        this.pagePath = pagePath;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public String getPagePath() {
        return pagePath;
    }
}
