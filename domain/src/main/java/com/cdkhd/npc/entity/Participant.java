package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 参与者
 *
 * @author sun
 * @date 2020/06/30
 */
@Setter
@Getter
@Entity
@Table( name ="participant" )
public class Participant extends BaseDomain {


    /**
     * 票数
     */
    @Column(name = "vote_num" )
    private Integer voteNum;

    /**
     * 账号
     */
    @OneToOne(targetEntity = Account.class, fetch = FetchType.LAZY,cascade  = CascadeType.ALL)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Account account;

    //关联候选人
    @ManyToOne(targetEntity = Candidate.class)//, fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate", referencedColumnName = "id")
    private Candidate candidate;

    //关联活动
    @ManyToOne(targetEntity = VoteActivity.class)//, fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_activity", referencedColumnName = "id")
    private VoteActivity voteActivity;

}
