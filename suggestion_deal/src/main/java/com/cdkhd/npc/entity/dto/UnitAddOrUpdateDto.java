package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitAddOrUpdateDto extends BaseDto {

    //单位名称
    private String name;

    //联系方式
    private String mobile;

    // 备注情况
    private String comment;

    //地址
    private String address;

    //单位业务
    private String business;

    //经度
    private String longitude;

    //纬度
    private String latitude;

    //单位图片
    private String avatar;
}
