package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.StatusEnum;
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
@ToString
@Entity
@Table ( name ="unit" )
public class Unit extends BaseDomain {

	//单位名称
	private String name;

	//联系电话
	@Column(unique = true)
	private String mobile;

	//单位状态  1、正常 2、锁定
	private Byte status = StatusEnum.ENABLED.getValue();

	// 备注情况
	private String comment;

	//单位地址
	private String address;

	//单位业务
	@OneToOne(targetEntity=SuggestionBusiness.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "business", referencedColumnName = "id")
	private SuggestionBusiness suggestionBusiness;

	//经度
	private String longitude;

	//纬度
	private String latitude;

	//办理单位基本信息
	@OneToMany(targetEntity = UnitUser.class, mappedBy = "unit", orphanRemoval = true)
	private Set<UnitUser> unitUser;

	@Column(name = "is_del" )
	private Boolean isDel = false;

	@Column(name = "level" )
	private Byte level;

	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Area area;

	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "town", referencedColumnName = "id")
	private Town town;

	//单位照片
	@Column(name = "avatar" )
	private String avatar;
}
