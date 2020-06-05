package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description
 * @Author rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "handle_process")
public class HandleProcess extends BaseDomain {

    //办理时间
    @Column
    private Date handleTime;

    //流程描述
    @Column(name = "description")
    private String description;

    //流程图片存储在UnitImage中

    //办理单位办理记录
    @ManyToOne(targetEntity = UnitSuggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "unitSuggestion", referencedColumnName = "id")
    private UnitSuggestion unitSuggestion;
}
