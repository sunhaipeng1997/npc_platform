package com.cdkhd.npc.entity.dto;

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
public class AddOpinionDto {

    private String transUid;

    /**
     * 意见内容
     */
	private String content;

    /**
     * 接受代表 手机号码
     */
    private String receiver;

    /**
     * 图片
     */
    private List<String> images;

    /**
     * 等级
     */
    private Byte level;

    //所属镇
    private String areaName;

    //所属镇
    private String townName;
}
