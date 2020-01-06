package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
public class NpcMemberVo {

    //姓名
    private String name;

    //性别
    private Byte gender;

    //电话号码
    private String mobile;

    //邮箱
    private String email;

    //地址
    private String address;

    //生日
    private Date birthday;

    //职务类型
    private Byte type;

    //代表证号
    private String code;

    //头像
    private String avatar;

    //简介
    private String introduction;

    //备注
    private String comment;

    //民族
    private String nation;

    //是否能被提意见
    private Byte canOpinion;

    //是否删除
    private Byte isDel;

    //教育经历
    private String education;

    //职务
    private String jobs;

    //政治面貌
    private String political;

    //入党时间
    private Date joiningTime;

    public static NpcMemberVo convert(NpcMember npcMember) {
        NpcMemberVo vo = new NpcMemberVo();

        BeanUtils.copyProperties(npcMember, vo);

        return vo;
    }
}