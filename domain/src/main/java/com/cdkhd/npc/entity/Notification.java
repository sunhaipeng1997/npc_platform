package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.enums.NotificationStatusEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "notification")
public class Notification extends BaseDomain {

    //通知标题
    @Column(nullable = false)
    private String title;

    //签署部门
    @Column(name = "department" )
    private String department;

    //通知内容-是HTML富文本，将其设置为字符串大对象，并懒加载
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String content;

    //如果为公告，类型可取值为1和2，分别表示浮动公告和系统更新提示
    //如果为通知，类型可取值为3、4，分别通知的存档类别：人大、其他
    @Column(name = "type")
    private Byte type;

    //附件
    @OneToMany(cascade = CascadeType.ALL) //表示级练操作
    @JoinColumn(name = "notification_id") //表示对应子表的关联外键，如果不使用这个注解则需要创建中间表
    private List<Attachment> attachments = new ArrayList<>();

    //是否为全局公告
    private boolean isBillboard = false;

    //接收人
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "notification_npcMember_relation",
            joinColumns = {
                    @JoinColumn(name = "notification_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "npcmember_id", referencedColumnName = "id")
            }
    )
    private Set<NpcMember> receivers = new HashSet<>();

    //通知状态
    @Column(name = "status" )
    private Integer status = NotificationStatusEnum.DRAFT.ordinal();

    //审核人
    @OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_npcMember", referencedColumnName = "id")
    private NpcMember reviewer;


    //区(县)
    @OneToOne(targetEntity=Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area",referencedColumnName = "id")
    private Area area;

    //镇（乡）
    @OneToOne(targetEntity=Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town",referencedColumnName = "id")
    private Town town;

    @Column(name = "level" )
    private Byte level;

    //通知是否已经发布
    private boolean published;

    //发布时间
    @Temporal(TemporalType.TIMESTAMP)
    private Date publishAt;

    //TODO 审核与操作记录相关的字段要分离到单独的一张表中去

    //审核人的反馈意见
    @Column(name = "feedback" )
    private String feedback;

    //审核人员查看状态
    private int view;

}
