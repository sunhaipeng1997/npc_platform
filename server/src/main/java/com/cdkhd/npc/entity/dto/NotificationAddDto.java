package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class NotificationAddDto extends BaseDto {
    private String title;

    private String department;

    private String content;

    private Byte type;

    private Set<String> attachmentsUid;

    //是否为公告
    private boolean isBillboard;

    private String tags;

    //接收人
    private Set<String> receiversUid;
}
