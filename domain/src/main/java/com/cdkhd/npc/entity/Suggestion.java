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
 * @Author rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "suggestion")
public class Suggestion extends BaseDomain {

    //建议标题
    @Column(name = "title")
    private String title;

    //建议内容
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content")
    private String content;

    /**
     * ---------建议状态
     * 撤回	0
     * 未提交	1
     * 已提交审核	2
     * 已提交政府	3
     * 已转交办理单位	4
     * 办理中	5
     * 办理完成	6
     * 办结	7
     * 自行办理	8
     * 审核失败	-1
     */
    @Column(name = "status")
    private Byte status;

    //领衔人手机号
    @Column(name = "mobile")
    private String mobile;

    //提出时间
    @Column(name = "raise_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date raiseTime;

    //审核原因
    @Column(name = "reason")
    private String reason;

    //审核原因
    @Column(name = "audit_reason")
    private String auditReason;

    //审核时间
    @Column(name = "audit_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date auditTime;

    //转办次数
    @Column(name = "convey_times")
    public Integer conveyTimes = 0;

    //延期次数
    @Column(name = "delay_times")
    public Integer delayTimes = 0;

    //转办时间
    @Column(name = "convey_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date conveyTime;

    //是否接受
    @Column(name = "accept")
    private Boolean accept;

    //拒绝原因
    @Column(name = "refusal_reason")
    private String refusalReason;

    //单位拒绝次数
    @Column(name = "refuse_times")
    private Integer refuseTimes;

    //单位接受时间
    @Column(name = "accept_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date acceptTime;

    //办理次数，主要是记录重新办理
    @Column(name = "deal_times")
    private Integer dealTimes;

    //办完时间
    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishTime;

    //办结时间
    @Column(name = "accomplish_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date accomplishTime;

    //是否催办
    @Column(name = "urge")
    private Boolean urge = false;

    //催办等级
    @Column(name = "urge_level")
    private Integer urgeLevel = 0;

    //是否快到期了
    @Column(name = "close_deadline")
    private Boolean closeDeadLine = false;

    //是否超期了
    @Column(name = "exceed_limit")
    private Boolean exceedLimit = false;

    //审核人员是否查看
    @Column(name = "view")
    private Boolean view = false;

    //办理单位办完建议后，代表是否查看
    @Column(name = "doneView")
    private Boolean doneView = false;

    //政府是否查看
    @Column(name = "govView")
    private Boolean govView = false;

    //是否删除
    @Column(name = "del")
    private Boolean isDel = false;

    //每次提交的uid
    @Column(name = "trans_uid")
    private String transUid;

    //建议级别 1 镇 2 区
    @Column(name = "level")
    private Byte level;

    //前端是否显示撤回按钮，默认为true
    @Column(name = "can_revoke")
    private Boolean canOperate = true;

    //转办人
    @OneToOne(targetEntity = GovernmentUser.class, fetch = FetchType.LAZY)
    private GovernmentUser governmentUser;

    //建议图片
    @OneToMany(targetEntity = SuggestionImage.class, mappedBy = "suggestion")
    private Set<SuggestionImage> suggestionImages;

    //政府转办记录
    @OneToMany(targetEntity = ConveyProcess.class, mappedBy = "suggestion")
    private Set<ConveyProcess> conveyProcesses;

    //延期申请记录
    @OneToMany(targetEntity = DelaySuggestion.class, mappedBy = "suggestion")
    private Set<DelaySuggestion> delaySuggestions;

    //办理单位办理记录
    @OneToMany(targetEntity = UnitSuggestion.class, mappedBy = "suggestion")
    private Set<UnitSuggestion> unitSuggestions;

    //建议回复
    @OneToMany(targetEntity = SuggestionReply.class, mappedBy = "suggestion", orphanRemoval = true)
    private Set<SuggestionReply> replies = new HashSet<>();

    //建议评价
    @OneToMany(targetEntity = Appraise.class, mappedBy = "suggestion", orphanRemoval = true)
    private Set<Appraise> appraises;

    //催办记录
    @OneToMany(targetEntity = Urge.class, mappedBy = "suggestion")
    private Set<Urge> urges;

    //办理结果
    @OneToMany(targetEntity = Result.class, mappedBy = "suggestion")
    private Set<Result> results;

    //附议
    @OneToMany(targetEntity = Seconded.class, mappedBy = "suggestion")
    private Set<Seconded> secondedSet;

    //对应的区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //对应的镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    //对应的小组
    @ManyToOne(targetEntity = NpcMemberGroup.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "npcMemberGroup", referencedColumnName = "id")
    private NpcMemberGroup npcMemberGroup;

    //实际的审核人员
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    private NpcMember auditor;

    //领衔人
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    private NpcMember leader;

    //提出代表
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    private NpcMember raiser;

    //建议类型
    @ManyToOne(targetEntity = SuggestionBusiness.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion_business", referencedColumnName = "id")
    private SuggestionBusiness suggestionBusiness;

    //办理单位
    @ManyToOne(targetEntity = Unit.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "unit", referencedColumnName = "id")
    private Unit unit;

    //建议预计办理完成时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expect_date")
    private Date expectDate;


}
