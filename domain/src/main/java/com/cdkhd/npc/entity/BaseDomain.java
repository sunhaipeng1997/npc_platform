package com.cdkhd.npc.entity;

import com.cdkhd.npc.util.SysUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
@MappedSuperclass
public class BaseDomain implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 数据生成时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime = new Date();

    // 数据的唯一标识符
    @Column(unique = true, nullable = false)
    private String uid = SysUtil.uid();

    public BaseDomain() {
    }

    public BaseDomain(String uid) {
        this.uid = uid;
    }
}
