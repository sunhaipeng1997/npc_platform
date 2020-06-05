package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 由于微信小程序上传图片一次请求只能上传一张图，
 * 故在第一次上传时生成Result，
 * 上传后续图片时，表示为其添加图片。
 */
@Getter
@Setter
public class ResultAddDto {
    //办理结果所属的UnitSuggestion的uid
    private String unitSugUid;

    //结果描述
    private String result;

    //结果图片
    private MultipartFile image;
}
