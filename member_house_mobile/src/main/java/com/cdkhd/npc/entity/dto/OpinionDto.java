package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class OpinionDto extends PageDto {

    //状态
    private Byte status;
}
