package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Author  ly
 * @Date 2019-01-06
 */

@Setter
@Getter
@Entity
@Table ( name ="suggestion_business" )
public class SuggestionBusiness extends BaseDomain {

    //类型名称
   	@Column(name = "name" )
	private String name;

    //排序号
    @Column(name = "sequence" )
    private Integer sequence;

    //逻辑删除
    @Column(name = "is_del" )
    private Boolean isDel = false;

    //备注
    @Column(name = "remark" )
    private String remark;

    @OneToMany(targetEntity = Suggestion.class, mappedBy = "suggestionBusiness", orphanRemoval = true)
    private Set<Suggestion> suggestions = new HashSet<>();

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

}
