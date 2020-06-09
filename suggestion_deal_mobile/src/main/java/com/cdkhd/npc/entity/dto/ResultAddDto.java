package com.cdkhd.npc.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultAddDto {
    //办理结果所属的UnitSuggestion的uid
    private String unitSugUid;

    //结果描述
    private String result;

    //结果图片
    private List<String> imageUrls;
}
