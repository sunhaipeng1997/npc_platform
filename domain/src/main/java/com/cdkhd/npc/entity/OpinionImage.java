package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @创建人
 * @创建时间 2018/9/27
 * @描述
 */
@Getter
@Setter
@Entity
@Table(name = "opinion_image")
public class OpinionImage extends BaseDomain{

    //图片
    private String picture;

    // 关联的意见
    @ManyToOne(targetEntity = Opinion.class, fetch = FetchType.LAZY)
    private Opinion opinion;

}
