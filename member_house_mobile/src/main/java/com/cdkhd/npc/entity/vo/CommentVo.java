package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class CommentVo extends BaseVo {

    private String name;

    //回复列表
    private String desc;

    public static CommentVo convert(String uid, String name, String desc) {
        CommentVo vo = new CommentVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setDesc(desc);
        return vo;
    }
}
