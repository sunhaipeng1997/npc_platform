package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;

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
     * 接受代表
     */
    private String receiver;

    /**
     * 图片
     */
    private MultipartFile image;

    /**
     * 等级
     */
    private Byte level;
}
