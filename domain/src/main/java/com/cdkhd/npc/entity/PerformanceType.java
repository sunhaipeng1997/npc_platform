package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.StatusEnum;
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
    private Byte status = StatusEnum.ENABLED.getValue();

    //排序号
    @Column(name = "sequence" )
    private Integer sequence;

    //逻辑删除
    @Column(name = "is_del" )
    private Boolean isDel = false;

    //备注
    @Column(name = "remark" )
    private String remark;

    //是否默认的类型，默认的不可删除
    @Column(name = "is_default" )
    private Boolean isDefault = false;

}
