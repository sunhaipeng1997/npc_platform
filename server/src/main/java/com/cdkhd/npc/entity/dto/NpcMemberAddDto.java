package com.cdkhd.npc.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class NpcMemberAddDto {

    //代表uid，修改代表时使用
    private String uid;

    //姓名
    @NotBlank
    private String name;

    @NotNull
    //电话
    private String mobile;

    //头像
    private String avatar;

    //邮箱
    private String email;

    //地址
    private String address;

    //生日
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    //性别
    private Byte gender;

    //任职类型
    private Byte type;

    //代表证号
    private String code;

    //简介
    private String introduction;

    //备注
    private String comment;

    //民族
    private String nation;

    //教育经历
    private String education;

    //政治面貌
    private String political;

    //任职届期
    private Set<String> sessionUids = new HashSet<>();

    /*
    * 工作单位uid
    * 如果是镇代表，则为小组uid；如果是区代表，则为镇uid
    */
    String workUnitUid;
}
