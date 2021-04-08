package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@Entity
@Table ( name ="study" )
public class Study extends BaseDomain {

    //学习资料名字
   	@Column(name = "name" )
	private String name;

    //类型状态 1、启用 2、禁用
    @Column(name = "status" )
    private Byte status = 1;

    //排序号
    @Column(name = "sequence" )
    private Integer sequence;

    //备注
    @Column(name = "remark" )
    private String remark;

    @Column(name = "level" )
    private Byte level;

    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    //类型
    @ManyToOne(targetEntity = StudyType.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "study_type", referencedColumnName = "id")
    private StudyType studyType;

    //路径
    @Column(name = "url" )
    private String url;

}
