package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class NotificationPageDto extends PageDto {

    private String title;

    private String department;

    //是否为全局公告
    private boolean isBillboard;

    private Byte type;

    private Integer status;

    private Byte level;

}
