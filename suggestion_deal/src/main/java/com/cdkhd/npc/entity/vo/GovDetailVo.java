package com.cdkhd.npc.entity.vo;
/*
 * @description:政府详情视图对象
 * @author:liyang
 * @create:2020-05-20
 */

import com.cdkhd.npc.entity.Government;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Setter
@Getter
public class GovDetailVo extends BaseVo {

    //政府名称
    private String name;

    //描述
    private String description;

    //账号
    private String account;

    //手机号
    private String mobile;

    //政府地址
    private String address;

    //经度
    private String longitude;

    //纬度
    private String latitude;

    //状态 1：正常 2：锁定
    private Byte status;

    public static GovDetailVo convert(Government government){
        GovDetailVo govDetailVo = new GovDetailVo();
        BeanUtils.copyProperties(government, govDetailVo);
        return govDetailVo;
    }
}
