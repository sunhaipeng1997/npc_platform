package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="opinion" )
public class Opinion extends BaseDomain {

    /**
     * 意见内容
     */
   	@Column(name = "content" )
	private String content;

    /**
     * 状态
     */
   	@Column(name = "status" )
	private Byte status;

    /**
     * 是否查看
     */
   	@Column(name = "view" )
	private Boolean view = false;

    /**
     * 接受代表
     */
    @OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver", referencedColumnName = "id")
    private NpcMember receiver;

    /**
     * 提出人账号
     */
    @ManyToOne(targetEntity=Account.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender", referencedColumnName = "id")
    private Account sender;

    @OneToMany(targetEntity = OpinionReply.class, mappedBy = "opinion", orphanRemoval = true)
    private Set<OpinionReply> replies = new HashSet<>();

    @OneToMany(targetEntity = OpinionImage.class, mappedBy = "opinion", orphanRemoval = true)
    private Set<OpinionImage> images = new HashSet<>();


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
