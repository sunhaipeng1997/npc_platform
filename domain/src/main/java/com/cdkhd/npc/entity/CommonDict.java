package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @描述 公共码表
 */

@Getter
@Setter
@Entity
@Table(name = "common_dict")
public class CommonDict extends BaseDomain{

    //码表code
    @Column(nullable = false)
    private String code;

    //码表名称
    @Column(nullable = false)
    private String name;

    //码表类型
    @Column(nullable = false)
    private String type;

    //码表类型名称
    @Column(nullable = false)
    private String typeName;

    //码表是否删除
    @Column(nullable = false)
    private Boolean isDel = false;

    //码表是否可维护
    @Column(nullable = false)
    private Boolean isMaintain = false;

    //备注
    private String remark;
}
