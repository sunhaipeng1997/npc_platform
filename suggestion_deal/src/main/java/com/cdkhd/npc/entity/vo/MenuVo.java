package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Menu;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class MenuVo extends BaseVo {
    //菜单名称
    private String name;

    //菜单图标
    private String icon;

    //页面跳转的url地址
    private String url;

    public static MenuVo convert(Menu menu) {
        MenuVo vo = new MenuVo();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }
}
