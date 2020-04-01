package com.cdkhd.npc.entity.dto;

import com.alibaba.fastjson.JSONArray;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class UserInfoDto extends BaseDto {

    //姓名
    private String name;

    //性别
    private Byte gender;

    private Integer age;

    //手机号
    private String mobile;

    //短信验证码
    private String verificationCode;

    //所属区
    private String areaUid;

    //所属镇
    private String townUid;

    //所属村
    private String villageUid;

    //任职情况
    private JSONArray workInfo;

    //微信昵称、临时code和加密数据以及加密算法向量
    private String nickName;
    private String code;
    private String encryptedData;
    private String iv;
}
