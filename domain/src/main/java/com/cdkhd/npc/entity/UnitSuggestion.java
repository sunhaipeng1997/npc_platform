package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * 主要针对对象为办理单位
 * 办理记录
 */
@Setter
@Getter
@Entity
@Table(name = "unit_suggestion")
public class UnitSuggestion extends BaseDomain {

    //办理单位性质 1 主办单位  2 协办单位
    private Byte type;

    //办理单位是否接受
    private Boolean accept;

    //办理单位拒绝原因
    @Column(name = "refuse_reason")
    private String refuseReason;

    //办理单位拒绝次数
    @Column(name = "refuse_times")
    private Integer refuseTimes = 0;

    //办理单位收到时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "receive_time")
    private Date receiveTime;

    //办理单位收到时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "accept_time")
    private Date acceptTime = new Date();

    //办理单位办理次数
    @Column(name = "deal_times")
    private Integer dealTimes = 0;

    //办理单位办完时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finish_time")
    private Date finishTime;

    //办理单位，是否处理完成（办理完成，拒绝完成）
    private Boolean finish = false;

    //政府处理过程 1 未处理 2 已重新分配 3 无需重新分配
    private Byte status = 1;

    //当前办理人员
    @OneToOne(targetEntity = UnitUser.class, fetch = FetchType.LAZY)
    private UnitUser unitUser;

    //单位信息
    @OneToOne(targetEntity = Unit.class, fetch = FetchType.LAZY)
    private Unit unit;

    //办理结果 协办单位反馈给主办单位的结果说明
    @OneToOne(targetEntity = Result.class, fetch = FetchType.LAZY)
    private Result result;

    // 转交的政府人员id
    @OneToOne(targetEntity = GovernmentUser.class, fetch = FetchType.LAZY)
    private GovernmentUser governmentUser;

    //办理流程
    @OneToMany(targetEntity = HandleProcess.class, mappedBy = "unitSuggestion")
    private Set<HandleProcess> processes;

    // 建议
    @ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion", referencedColumnName = "id")
    private Suggestion suggestion;

}

