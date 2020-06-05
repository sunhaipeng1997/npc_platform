package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 由于微信小程序上传图片一次请求只能上传一张图，
 * 故在第一次上传时生成HandleProcess，并返回其uid，
 * 上传后续图片时，带上该uid参数，表示为其添加图片
 * （注：此处的uid参数在BaseDto中）。
 */
@Getter
@Setter
public class HandleProcessAddDto extends BaseDto {
    //办理过程所属的UnitSuggestion的uid
    private String unitSugUid;

    //流程办理时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date handleTime;

    //过程描述
    private String description;

    //过程图片
    private MultipartFile image;
}
