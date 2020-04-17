package com.cdkhd.npc.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountVo {
    String month;

    Integer count;

    public CountVo(String month, Long count) {
        this.month = month;
        this.count = count.intValue();
    }
}
