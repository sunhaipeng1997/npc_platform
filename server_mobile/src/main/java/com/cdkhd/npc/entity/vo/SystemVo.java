package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.vo.BaseVo;
import org.springframework.beans.BeanUtils;

public class SystemVo extends BaseVo {

    //系统名称
    private String name;

    //图标
    private String svg;

    //描述
    private String description;

    // 描述
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    public static SystemVo convert(Systems systems) {
        SystemVo vo = new SystemVo();
        BeanUtils.copyProperties(systems, vo);
        return vo;
    }
}
