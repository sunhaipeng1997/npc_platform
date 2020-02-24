package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Setter
@Getter
public class GroupMemberVo extends BaseVo {

    //代表名称
    private String name;

    //代表出生日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date birthday;

    //性别 0 表示女  1表示男
    private int gender;

    //手机号
    private String mobile;

    //代表正面照
    private String avatar;

    //代表简介
    private String introduction;

    //履职状态  0 表示任职  -1 表示往届
    private int status;

    public static GroupMemberVo convert(NpcMember npcMember) {
        GroupMemberVo vo = new GroupMemberVo();
        BeanUtils.copyProperties(npcMember, vo);
        return vo;
    }
}
