package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 办理单位用来存储图片的实体。
 * 包括：办理流程图片，办理结果图片
 */
@Getter
@Setter
@Entity
@Table(name = "unit_image")
public class UnitImage extends BaseDomain {
    //图片url
    @Column
    private String url;

    //图片类型，参见枚举ImageTypeEnum
    @Column
    private Byte type;
    //关联的实体id
    @Column
    private Long belongToId;
}
