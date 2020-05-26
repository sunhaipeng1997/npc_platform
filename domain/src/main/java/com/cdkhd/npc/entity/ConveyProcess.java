package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.ConveyStatusEnum;
import com.cdkhd.npc.enums.GovDealStatusEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * 主要针对对象为政府
 * 转办记录
 */
@Getter
@Setter
@Entity
@Table(name = "convey_process")
public class ConveyProcess extends BaseDomain {

    //转办结果 0 转办中  1、转办成功  2、转办失败
    @Column(nullable = false)
    private Byte status = ConveyStatusEnum.CONVEYING.getValue();

    //政府处理状态  0 未处理 1 已重新分配 2 无需重新分配
    @Column(name = "deal_status")
    private Byte dealStatus;

    //本次转办是否处理完成  完成：单位接受了转办  或者  单位拒绝，政府已经重新转办或者选择不转办
    @Column(name = "deal_done")
    private Boolean dealDone;

    //转办次数
    @Column(name = "convey_times")
    private Integer conveyTimes = 0;

    //转办时间
    @Column(name = "convey_time")
    private Date conveyTime = new Date();

    //拒绝原因
    private String remark;

    //办理单位类型 1、主办单位  2、协办单位
    @Column(nullable = false)
    private Byte type;

    //办理单位是否已读，1：已读，0：未读
    @Column
    private Byte view;

    //办理单位回复是否已读，1：已读，0：未读
    @Column
    private Byte  myView;

    //目标办理单位
    @OneToOne(targetEntity = Unit.class, fetch = FetchType.LAZY)
    private Unit unit;

    //转办人
    @OneToOne(targetEntity = GovernmentUser.class, fetch = FetchType.LAZY)
    private GovernmentUser governmentUser;

    //对应的建议
    @ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion", referencedColumnName = "id")
    private Suggestion suggestion;

}
