package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 订阅活动
 *
 * @author sun
 * @date 2020/06/30
 */
@Setter
@Getter
@Entity
@Table( name ="subscribe_activity" )
public class SubscribeActivity extends BaseDomain {


    /**
     * 账户
     */

    @OneToOne(targetEntity = Account.class, fetch = FetchType.LAZY,cascade  = CascadeType.ALL)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Account account;



    /**
     * 投票活动
     */
    @ManyToOne(targetEntity = VoteActivity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_activity", referencedColumnName = "id")
    private VoteActivity voteActivity;
}
