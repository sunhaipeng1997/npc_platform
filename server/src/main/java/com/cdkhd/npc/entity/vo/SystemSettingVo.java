package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class SystemSettingVo extends BaseVo {
    //1.履职审核小组审核开关
    private Boolean performanceGroupAudit;

    //2.展示履职信息时是否展示下级单位履职
    private Boolean showSubPerformance;

    //3.选民是否可以向非本组代表提意见
    private Boolean voterOpinionToAll;

    //4.代表是否可以向代表提意见
    private Boolean memberOpinionToMember;

    //5.是否开启全局浮动公告
    private Boolean floatNotice;

    //6.是否开启新闻推送(微信端通过服务号，安卓端通过消息通知)
    private Boolean pushNews;

    //7.是否开启学习活动推送
    private Boolean pushStudy;

    //8.是否开启软件更新提示
    private Boolean pushUpdate;

    //9 .是否开启软件快捷办公
    private Boolean quickWork;

    public static SystemSettingVo convert(SystemSetting systemSetting) {
        SystemSettingVo vo = new SystemSettingVo();
        BeanUtils.copyProperties(systemSetting, vo);
        return vo;
    }
}
