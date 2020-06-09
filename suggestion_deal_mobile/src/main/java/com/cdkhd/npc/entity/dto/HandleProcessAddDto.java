package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

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
    private List<String> imageUrls;
}
