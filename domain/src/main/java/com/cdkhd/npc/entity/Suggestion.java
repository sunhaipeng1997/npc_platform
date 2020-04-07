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
@Table ( name ="suggestion" )
public class Suggestion extends BaseDomain {

   	@Column(name = "title" )
	private String title;

	@ManyToOne(targetEntity = SuggestionBusiness.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "suggestion_business", referencedColumnName = "id")
	private SuggestionBusiness suggestionBusiness;

   	@Column(name = "content" )
	private String content;

   	//提出代表
	@ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
	private NpcMember raiser;

   	//建议回复
	@OneToMany(targetEntity = SuggestionReply.class, mappedBy = "suggestion", orphanRemoval = true)
	private Set<SuggestionReply> replies = new HashSet<>();

	/**
	 * ---------建议状态
	 未提交	1
	 已提交审核	2
	 已提交政府	3
	 已转交办理单位	4
	 办理中	5
	 办理完成	6
	 办结	7
	 自行办理	8
	 审核失败	-1
	 */
   	@Column(name = "status" )
	private Byte status;

	//领衔人
	@ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
	private NpcMember leader;

	//领衔人手机号
   	@Column(name = "mobile" )
	private String mobile;

	//提出时间
   	@Column(name = "raise_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date raiseTime;

   	//实际的审核人员
	@ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
	private NpcMember auditor;

   	@Column(name = "reason" )
	private String reason;

   	//审核原因
   	@Column(name = "audit_reason" )
	private String auditReason;

   	//审核时间
   	@Column(name = "audit_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date auditTime;

   	@Column(name = "convey_id" )
	private String conveyId;

   	@Column(name = "convey_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date conveyTime;

   	@Column(name = "accept" )
	private Boolean accept;

   	@Column(name = "refusal_reason" )
	private String refusalReason;

   	@Column(name = "refuse_times" )
	private Integer refuseTimes;

   	@Column(name = "unit" )
	private String unit;

   	@Column(name = "accept_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date acceptTime;

   	@Column(name = "deal_times" )
	private Integer dealTimes;

   	@Column(name = "finish_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishTime;

   	@Column(name = "accomplish_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date accomplishTime;

   	@Column(name = "urge" )
	private Integer urge;

   	//审核人员是否查看
   	@Column(name = "view" )
	private Boolean view = false;

	@Column(name = "del" )
	private Boolean isDel = false;

	//每次提交的uid
	@Column(name = "trans_uid" )
	private String transUid;

	@Column(name = "level" )
	private Byte level;

	//是否可操作
	private Boolean canOperate = false;

	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Area area;

	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "town", referencedColumnName = "id")
	private Town town;

    @ManyToOne(targetEntity = NpcMemberGroup.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "npcMemberGroup", referencedColumnName = "id")
    private NpcMemberGroup npcMemberGroup;


	//建议图片
	@OneToMany(targetEntity = SuggestionImage.class, mappedBy = "suggestion")
	private Set<SuggestionImage> suggestionImages;
}
