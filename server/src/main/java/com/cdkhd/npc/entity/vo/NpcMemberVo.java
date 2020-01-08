package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.enums.GenderEnum;
import com.cdkhd.npc.enums.JobsEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String genderName;

    //电话号码
    private String mobile;

    //邮箱
    private String email;

    //地址
    private String address;

    //生日
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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

    //教育经历
    private String education;

    //职务
    private String jobs;

    //政治面貌
    private String political;

    //入党时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date joiningTime;

    //工作单位
    private String workUnit;

    //届期
    private String session;

    public static NpcMemberVo convert(NpcMember npcMember) {
        NpcMemberVo vo = new NpcMemberVo();
        BeanUtils.copyProperties(npcMember, vo);
        vo.setGenderName(GenderEnum.MALE.getName());
        vo.setWorkUnit(npcMember.getLevel().equals(LevelEnum.TOWN.getValue())?npcMember.getNpcMemberGroup().getName():npcMember.getTown().getName());
        vo.setJobs(JobsEnum.getName(npcMember.getJobs()));
        vo.setSession("其他");
        return vo;
    }
}
