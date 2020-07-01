package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 候选人
 *
 * @author sun
 * @date 2020/06/30
 */
@Setter
@Getter
@ToString
@Entity
@Table( name ="candidate" )
public class Candidate extends BaseDomain {


    /**
     * 代表id
     */
    @OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY,cascade  = CascadeType.ALL)
    @JoinColumn(name = "npcMember", referencedColumnName = "id")
    private NpcMember npcMember;

    /**
     * 浮动票数
     */
    @Column(name = "float_vote" )
    private Integer FloatVote;

    /**
     * 参选说明
     */
    @Column(name = "explanation" )
    private String explanation;

    //关联活动
    @ManyToOne(targetEntity = VoteActivity.class)//, fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_activity", referencedColumnName = "id")
    private VoteActivity voteActivity;
    //参与投票的人
    @OneToMany(targetEntity = Participant.class, mappedBy = "candidate")
    private Set<Participant> participants = new HashSet<>();
}
