package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="performance_type" )
public class PerformanceType extends BaseDomain {

    //类型名称
   	@Column(name = "name" )
	private String name;

    @Column(name = "level" )
    private Byte level;

    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    //类型状态 1、启用 2、禁用
    @Column(name = "status" )
    private Byte status = 1;

    //排序号
    @Column(name = "sequence" )
    private Integer sequence;

    //逻辑删除
    @Column(name = "is_del" )
    private Boolean isDel = false;

    //备注
    @Column(name = "remark" )
    private String remark;

}
