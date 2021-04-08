package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@Entity
@Table(name = "government_user")
public class GovernmentUser extends BaseDomain {
    /**
     * 1、正常
     * 2、锁定
     */
    @Column(name = "status")
    private Byte status = StatusEnum.ENABLED.getValue();

    /**
     * 等级，县/区后台管理员 or 镇后台管理员
     * 见com.cdkhd.npc.enums.LevelEnum
     */
    @Column(name = "level")
    private Byte level;

    //关联区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    /**
     * 账号表id
     */
    @OneToOne(targetEntity = Account.class, fetch = FetchType.LAZY)
    private Account account;

    @OneToOne(targetEntity = Government.class, fetch = FetchType.LAZY)
    private Government government;

}
