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
@ToString
@Entity
@Table ( name ="performance" )
public class Performance extends BaseDomain {

    //履职内容
   	@Column(name = "content" )
	private String content;

   	//我是否查看
   	@Column(name = "my_view" )
	private Byte myView;

   	//审核原因
   	@Column(name = "reason" )
	private String reason;

   	//状态
   	@Column(name = "status" )
	private Byte status;

   	//标题
   	@Column(name = "title" )
	private String title;

   	//类型
    @ManyToOne(targetEntity = PerformanceType.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_type", referencedColumnName = "id")
    private PerformanceType performanceType;

   	//审核人是否查看
   	@Column(name = "view" )
	private Byte view;

   	//履职时间
   	@Column(name = "work_at" )
	private Date workAt;

   	//审核人信息
    @ManyToOne(targetEntity = Account.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    //提出代表信息
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_member", referencedColumnName = "id")
    private NpcMember npcMember;

   	@Column(name = "level" )
	private Integer level;

    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

}
