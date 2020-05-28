package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * 建议延期申请记录
 */
@Setter
@Getter
@Entity
@Table(name = "delay_suggestion")
public class DelaySuggestion extends BaseDomain {

    //是否同意
    private Boolean accept;

    //申请原因
    private String reason;

    //延期次数次数
    @Column(name = "delay_times")
    private Integer delayTimes = 0;

    //预计延期时间
    @Column(name = "apply_time")
    private Date applyTime;

    //审批备注
    private String remark;

    //政府审批的实际延期时间
    @Column(name = "delay_time")
    private Date delayTime;

    // 建议
    @ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion", referencedColumnName = "id")
    private Suggestion suggestion;

    // 单位办理情况
    @ManyToOne(targetEntity = UnitSuggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "unitSuggestion", referencedColumnName = "id")
    private UnitSuggestion unitSuggestion;

}
