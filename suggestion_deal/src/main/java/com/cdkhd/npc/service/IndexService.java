package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface IndexService {

    //首页相关请求


    /**
     * 建议数量趋势图
     * @param userDetails
     * @return
     */
    RespBody getGovSugNumber(UserDetailsImpl userDetails);


    /**
     * 建议数量趋势图
     * @param userDetails
     * @return
     */
    RespBody getGovSugCount(UserDetailsImpl userDetails);

    /**
     * 建议类型统计图
     * @param userDetails
     * @return
     */
    RespBody sugGovBusinessLine(UserDetailsImpl userDetails);

    /**
     *  单位办理中的统计图
     * @param userDetails
     * @return
     */
    RespBody sugGovUnitDealingLine(UserDetailsImpl userDetails);


    /**
     * 单位办结的统计图
     * @param userDetails
     * @return
     */
    RespBody sugGovUnitCompletedLine(UserDetailsImpl userDetails);


    /**
    * @Description: 管理员获取本月新增建议数量
    * @Param:
    * @Return:
    * @Date: 2020/6/11
    * @Author: LiYang
    */
    RespBody adminGetSugNumber(UserDetailsImpl userDetails);

    /**
    * @Description: 人大后台管理员首页建议数量趋势图
    * @Param:
    * @Return:
    * @Date: 2020/6/11
    * @Author: LiYang
    */
    RespBody adminNewSugNum(UserDetailsImpl userDetails);

    /**
    * @Description: 人大后台管理员首页建议按类型统计图
    * @Param:
    * @Return:
    * @Date: 2020/6/12
    * @Author: LiYang
    */
    RespBody adminSugBusinessLine(UserDetailsImpl userDetails);


    /**
    * @Description: 人大后台管理员首页建议按下属机构分组统计新增建议
    * @Param:
    * @Return:
    * @Date: 2020/6/12
    * @Author: LiYang
    */
    RespBody adminSugNumGroupBySubordinate(UserDetailsImpl userDetails);
}
