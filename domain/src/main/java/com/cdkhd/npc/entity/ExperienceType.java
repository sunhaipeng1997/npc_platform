package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
@ToString
@Entity
@Table ( name ="experience_type" )
public class ExperienceType extends BaseDomain {

    //类型名称
   	@Column(name = "name" )
	private String name;

    @Column(name = "area" )
    private Integer area;

    @Column(name = "town" )
    private String town;

    //类型状态
    @Column(name = "status" )
    private String status;

    //类型状态
    @Column(name = "sequence" )
    private String sequence;


}
