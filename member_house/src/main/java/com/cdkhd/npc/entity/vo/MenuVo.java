package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Menu;
import com.cdkhd.npc.vo.BaseVo;
import org.springframework.beans.BeanUtils;

public class MenuVo extends BaseVo {
    //菜单名称
    private String name;

    //菜单图标
    private String icon;

    //页面跳转的url地址
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static MenuVo convert(Menu menu) {
        MenuVo vo = new MenuVo();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }
}
