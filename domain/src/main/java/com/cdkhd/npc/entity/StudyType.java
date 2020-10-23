package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@Entity
@Table ( name ="study_type" )
public class StudyType extends BaseDomain {

    /**
     * 学习类型名称
     */
   	@Column(name = "name" )
	private String name;

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

    //关联区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    /**
     * 等级
     */
    @Column(name = "level" )
	private Byte level;

    /**
     * 学习资料
     */
    @OneToMany(mappedBy = "studyType",targetEntity = Study.class, fetch = FetchType.LAZY)
    private Set<Study> studies;

}
