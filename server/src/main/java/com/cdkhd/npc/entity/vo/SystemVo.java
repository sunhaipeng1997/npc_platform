package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class SystemVo extends BaseVo {

    //系统名称
    private String name;

    //图标
    private String svg;

    //描述
    private String description;

    // 描述
    private String url;


    public static SystemVo convert(Systems systems) {
        SystemVo vo = new SystemVo();
        BeanUtils.copyProperties(systems, vo);
        return vo;
    }
}
