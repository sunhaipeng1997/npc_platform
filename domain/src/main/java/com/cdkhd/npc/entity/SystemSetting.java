package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="system_setting" )
public class SystemSetting extends BaseDomain {

    //1.履职审核小组审核开关
    @Column(name = "performance_group_audit" )
	private Boolean performanceGroupAudit = true;

    //2.展示履职信息时是否展示下级单位履职
    @Column(name = "show_sub_performance" )
	private Boolean showSubPerformance = true;

    //3.选民是否可以向非本组代表提意见
    @Column(name = "voter_opinion_to_all" )
	private Boolean voterOpinionToAll = true;

    //4.代表是否可以向代表提建议
    @Column(name = "member_opinion_to_member" )
	private Boolean memberOpinionToMember = true;

    //5.是否开启全局浮动公告
    @Column(name = "float_notice" )
	private Boolean floatNotice = true;

    //6.是否开启移动端首页自定义快捷功能
    @Column(name = "quick_work" )
	private Boolean quickWork = true;

    //7.是否开启新闻推送(微信端通过服务号，安卓端通过消息通知)
    @Column(name = "push_news" )
	private Boolean pushNews = true;

    //8.是否开启学习活动推送
    @Column(name = "push_study" )
	private Boolean pushStudy = true;

    //9.是否开启软件更新提示
    @Column(name = "push_update" )
	private Boolean pushUpdate = true;

    @Column(name = "level" )
    private Byte level;

    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

}
