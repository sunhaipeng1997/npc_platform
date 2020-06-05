package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;

public interface GovSuggestionService {

    /**
     * 条件查询政府的建议列表
     * */
    RespBody getGovSuggestion(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto);

    /**
     * 转办单位
     * @param userDetails
     * @param conveySuggestionDto
     * @return
     */
    RespBody conveySuggestion(MobileUserDetailsImpl userDetails, ConveySuggestionDto conveySuggestionDto);

    /**
     * 转办单位
     * @param userDetails
     * @param delaySuggestionDto
     * @return
     */
    RespBody delaySuggestion(MobileUserDetailsImpl userDetails, DelaySuggestionDto delaySuggestionDto);

    /**
     * 调整办理单位
     * @param userDetails
     * @param adjustConveyDto
     * @return
     */
    RespBody adjustConvey(MobileUserDetailsImpl userDetails, AdjustConveyDto adjustConveyDto);


    /**
     * 申请调整的建议
     **/
    RespBody applyConvey(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto);

    /**
     * 申请延期的建议
     **/
    RespBody applyDelay(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto);

    /**
     * 催办建议
     * @param userDetails
     * @param levelDto
     * @return
     */
    RespBody urgeSug(MobileUserDetailsImpl userDetails, LevelDto levelDto);

    /**
     * 根据建议uid获取建议详情
     * @param baseDto
     * @return
     */
    RespBody getSuggestionDetail(BaseDto baseDto);


    /**
     * 查看申请延期的建议详情
     * @param baseDto
     * @return
     */
    RespBody getDelaySugDetail(BaseDto baseDto);


    /**
     * 查看申请调整单位的建议详情
     * @param baseDto
     * @return
     */
    RespBody getAdjustSugDetail(BaseDto baseDto);
}
