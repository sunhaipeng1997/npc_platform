package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="performance_image" )
public class PerformanceImage extends BaseDomain {

    // 数据的唯一标识符
    @ManyToOne(targetEntity = Performance.class, fetch = FetchType.LAZY)
    private Performance performance;

    //图片uid
    @Column(name = "trans_uid" )
    private String transUid;

    //图片路径
    @Column(name = "url" )
    private String url;
}
