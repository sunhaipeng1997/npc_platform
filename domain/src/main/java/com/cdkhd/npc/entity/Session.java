package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
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
@Table ( name ="session" )
public class Session extends BaseDomain {

	/**
	 * 届期名称
	 */
   	@Column(name = "name" )
	private String name;

	/**
	 * 届期开始时间
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "start_date" )
    @Temporal(TemporalType.DATE)
    private Date startDate;

	/**
	 * 届期结束时间
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "end_date" )
    @Temporal(TemporalType.DATE)
	private Date endDate;

   	//备注
   	@Column(name = "remark" )
	private String remark;

   	//届期的等级，区的届期/镇的届期
	@Column
   	private Byte level;

	//关联区
	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

	//关联镇
	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    /**
     * 届次信息
     */
    @ManyToMany(targetEntity = NpcMember.class)
    @JoinTable(
            name = "npc_member_session_mid",
            joinColumns = {
                    @JoinColumn(name = "session_id", referencedColumnName = "id", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "npc_member_id", referencedColumnName = "id", nullable = false)
            }
    )
    private Set<NpcMember> npcMembers = new HashSet<>();

}
