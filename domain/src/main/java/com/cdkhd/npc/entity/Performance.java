package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.PerformanceStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@Entity
@Table(name = "performance")
public class Performance extends BaseDomain {

    //履职内容
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content" )
	private String content;

   	//我是否查看
   	@Column(name = "my_view" )
	private Boolean myView = true;

   	//审核原因
   	@Column(name = "reason" )
	private String reason;

   	//状态  0、撤回 1、未提交  2 待审核  3、审核通过  -1、审核失败
   	@Column(name = "status" )
	private Byte status = PerformanceStatusEnum.SUBMITTED_AUDIT.getValue();

   	//标题
   	@Column(name = "title" )
	private String title;

   	//类型
    @ManyToOne(targetEntity = PerformanceType.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_type", referencedColumnName = "id")
    private PerformanceType performanceType;

   	//审核人是否查看
   	@Column(name = "view" )
	private Boolean view = false;

   	//履职时间
   	@Column(name = "work_at" )
    @Temporal(TemporalType.DATE)
    private Date workAt;

   	//审核时间
   	@Column(name = "audit_at" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date auditAt;

   	//审核人信息
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor", referencedColumnName = "id")
    private NpcMember auditor;

    //提出代表信息
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_member", referencedColumnName = "id")
    private NpcMember npcMember;

   	@Column(name = "level" )
	private Byte level;

    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    @ManyToOne(targetEntity = NpcMemberGroup.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "npcMemberGroup", referencedColumnName = "id")
    private NpcMemberGroup npcMemberGroup;

    //图片uid
    @Column(name = "trans_uid" )
    private String transUid;

    //是否删除
    @Column(name = "is_del" )
    private Boolean isDel = false;

    //履职图片
    @OneToMany(targetEntity = PerformanceImage.class, mappedBy = "performance")
    private Set<PerformanceImage> performanceImages;

    //是否能进行操作(修改和撤回)
    private Boolean canOperate = true;
}
