package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * @创建人
 * @创建时间 2018/9/27
 * @描述
 */
@Entity
@Setter
@Getter
@Table(name = "suggestion_image")
public class SuggestionImage extends BaseDomain{

    @ManyToOne(targetEntity = Suggestion.class,fetch = FetchType.LAZY)
    private Suggestion suggestion;

    //图片路径
    @Column(name = "url" )
    private String url;

    //图片uid
    @Column(name = "trans_uid" )
    private String transUid;

}
