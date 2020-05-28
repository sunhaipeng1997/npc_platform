package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.HandleProcess;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HandleProcessVo {
    private String description;

    private List<String> images;

    public static HandleProcessVo convert(HandleProcess hp) {
        return new HandleProcessVo();
    }
}
