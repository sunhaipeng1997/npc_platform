package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface HomePageService {

    /**
     * 获取今日新增数量
     * @param userDetails
     * @return
     */
    RespBody getTodayNumber(UserDetailsImpl userDetails);

    /**
     * 代表建议曲线图
     * @param userDetails
     * @return
     */
    RespBody drawSuggestion(UserDetailsImpl userDetails);

    /**
     * 代表收到的意见曲线图
     * @param userDetails
     * @return
     */
    RespBody drawOpinion(UserDetailsImpl userDetails);

    /**
     * 代表履职曲线图
     * @param userDetails
     * @return
     */
    RespBody drawPerformance(UserDetailsImpl userDetails);

    /**
     * 代表履职类型数量柱状图
     * @param userDetails
     * @return
     */
    RespBody drawPerformanceType(UserDetailsImpl userDetails);

}
