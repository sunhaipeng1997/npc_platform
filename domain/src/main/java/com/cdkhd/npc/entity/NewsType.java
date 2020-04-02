package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;

import com.cdkhd.npc.enums.StatusEnum;
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
@Table ( name ="news_type" )
public class NewsType extends BaseDomain {

   	@Column(name = "name" )
	private String name;

	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Area area;

	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "town", referencedColumnName = "id")
	private Town town;

	@Column(name = "level" )
	private Byte level;

    //类型状态
    @Column(name = "status" )
	private Byte status = StatusEnum.DISABLED.getValue();

    //类型顺序
    @Column(name = "sequence" )
	private Integer sequence;

   	@Column(name = "remark" )
	private String remark;
}
