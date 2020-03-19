package com.cdkhd.npc.entity.dto;

import com.alibaba.fastjson.JSONArray;
import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class NotificationAddDto extends BaseDto {
    private String title;

    private String department;

    private String content;

    private Byte type;

    private JSONArray attachmentsUid;

    //是否为公告
    private boolean isBillboard;

    //接收人
    private JSONArray receiversUid;
}
