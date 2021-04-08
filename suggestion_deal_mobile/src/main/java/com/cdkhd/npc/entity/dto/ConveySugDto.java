package com.cdkhd.npc.entity.dto;
/*
 * @description:建议评价模块持久层
 * @author:liyang
 * @create:2020-05-26
 */
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ConveySugDto {

    //建议uid
    private String uid;

    //主办单位
    private String mainUnit;

    //协办单位
    private List<String> sponsorUnits;
}
