package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.GovDealStatusEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    //办理单位是否查看
    @Column(name = "unit_view")
    private Boolean unitView = false;

    //代表处理完办理结果后，办理单位是否查看
    @Column(name = "completeView")
    private Boolean completeView = false;

    //办理单位收到时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "receive_time")
    private Date receiveTime;

    //办理单位接受时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "accept_time")
    private Date acceptTime;

    //办理单位办理次数
    @Column(name = "deal_times")
    private Integer dealTimes = 0;

    //办理单位办完时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finish_time")
    private Date finishTime;

    //办理单位，是否处理完成（办理完成，拒绝完成）
    private Boolean finish = false;

    //政府处理过程 未处理 已重新分配 无需重新分配
    private Byte status = GovDealStatusEnum.NOT_DEAL.getValue();

    //当前办理人员
    @OneToOne(targetEntity = UnitUser.class, fetch = FetchType.LAZY)
    private UnitUser unitUser;

    //单位信息
    @OneToOne(targetEntity = Unit.class, fetch = FetchType.LAZY)
    private Unit unit;

    //办理结果 协办单位反馈给主办单位的结果说明
    @OneToOne(targetEntity = Result.class, fetch = FetchType.LAZY, mappedBy = "unitSuggestion")
    private Result result;

    // 转交的政府人员id
    @OneToOne(targetEntity = GovernmentUser.class, fetch = FetchType.LAZY)
    private GovernmentUser governmentUser;

    //办理流程
    @OneToMany(targetEntity = HandleProcess.class, mappedBy = "unitSuggestion")
    private List<HandleProcess> processes = new ArrayList<>();

    // 建议
    @ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion", referencedColumnName = "id")
    private Suggestion suggestion;

    //延期申请记录
    @OneToMany(targetEntity = DelaySuggestion.class, mappedBy = "unitSuggestion")
    private Set<DelaySuggestion> DelaySuggestions;

    //单位申请的延期次数
    @Column(name = "delay_times")
    public Integer delayTimes = 0;

    //单位预计办理完成时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expect_date")
    private Date expectDate;

}

