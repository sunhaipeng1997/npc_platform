package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.AdjustConveyDto;
import com.cdkhd.npc.entity.dto.ConveySuggestionDto;
import com.cdkhd.npc.entity.dto.DelaySuggestionDto;
import com.cdkhd.npc.entity.dto.GovSuggestionPageDto;
import com.cdkhd.npc.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GovSuggestionService {

    /**
     * 条件查询政府的建议列表
     * */
    RespBody getGovSuggestion(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto);


    /**
     * 条件查询政府的建议列表
     * */
    void exportGovSuggestion(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, HttpServletRequest req, HttpServletResponse res);

    /**
     * 转办单位
     * @param userDetails
     * @param conveySuggestionDto
     * @return
     */
    RespBody conveySuggestion(UserDetailsImpl userDetails, ConveySuggestionDto conveySuggestionDto);

    /**
     * 转办单位
     * @param userDetails
     * @param delaySuggestionDto
     * @return
     */
    RespBody delaySuggestion(UserDetailsImpl userDetails, DelaySuggestionDto delaySuggestionDto);

    /**
     * 调整办理单位
     * @param userDetails
     * @param adjustConveyDto
     * @return
     */
    RespBody adjustConvey(UserDetailsImpl userDetails, AdjustConveyDto adjustConveyDto);


    /**
     * 申请调整的建议
     **/
    RespBody applyConvey(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto);

    /**
     * 申请延期的建议
     **/
    RespBody applyDelay(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto);

    /**
     * 催办建议
     * @param userDetails
     * @param baseDto
     * @return
     */
    RespBody urgeSug(UserDetailsImpl userDetails, BaseDto baseDto);
}
