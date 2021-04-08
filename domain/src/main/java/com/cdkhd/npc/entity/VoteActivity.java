package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 投票活动
 *
 * @author sun
 * @date 2020/06/30
 */
@Setter
@Getter
@Entity
@Table( name ="vote_activity" )
public class VoteActivity extends BaseDomain {







    /**
     * 活动名称
     */
    @Column(name = "vote_name" )
    private String voteName;

    /**
     * 说明
     */
    @Column(name = "explanation" )
    private String explanation;

    /**
     * 规则
     */
    @Column(name = "rule" )
    private String rule;

    /**
     *开始日期
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime ;

    /**
     *结束日期
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime ;

    /**
     * 票数（每人可以投几票）
     */
    @Column(name = "vote_num" )
    private Integer voteNum;

    /**
     * 1、单选
     * 2、多选
     */
    @Column(name = "vote_option" )
    private Boolean voteOption;

    /**
     * 1、单个活动只能投一次
     * 2、单个活动可以投多次
     */
    @Column(name = "vote_type" )
    private Byte voteType;


    /**
     * 投票频率（几天一次）
     */
    @Column(name = "vote_frequency" )
    private Integer voteFrequency;

    /**
     * 是否匿名
     *
     */
    @Column(name = "is_anonymous" )
    private Boolean isAnonymous;

    /**
     * 参与人数
     */
    @Column(name = "participation" )
    private Integer participation;

    /**
     * 投票人数
     */
    @Column(name = "turnout" )
    private Integer turnout;

    /**
     * 1、未开始
     * 2、进行中
     * 3、已结束
     */
    @Column(name = "activity_status" )
    private Byte activityStatus;

    /**
     * 整体浮动票数
     */
    @Column(name = "whole_float_vote" )
    private Integer wholeFloatVote;

    //参与投票的人
    @OneToMany(targetEntity = Participant.class, mappedBy = "voteActivity", orphanRemoval = true)
    private Set<Participant> votersSet = new HashSet<>();

    //订阅活动
    @OneToMany(targetEntity = SubscribeActivity.class, mappedBy = "voteActivity",orphanRemoval = true)
    private Set<SubscribeActivity> subscribeActivities = new HashSet<>();
    //参与人
    @OneToMany(targetEntity = Participant.class, mappedBy = "voteActivity",orphanRemoval = true)
    private Set<Participant> participants = new HashSet<>();


    /**
     *   等级
     *   1、镇代表
     *   2、区代表
     */
    @Column(name = "level" )
    private Byte level;

    //关联区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;



}
