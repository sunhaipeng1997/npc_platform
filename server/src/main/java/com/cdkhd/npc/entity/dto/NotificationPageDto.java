package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;

import java.util.Set;

public class NotificationPageDto extends PageDto {

    private String title;

    private String department;

    //是否为全局公告
    private boolean isBillboard;

}
