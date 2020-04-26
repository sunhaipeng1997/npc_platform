package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;

import java.util.Set;

public interface PerformanceService {

    //履职类型相关接口

    /**
     * 履职类型列表
     * @param userDetails
     * @return
     */
    RespBody performanceTypes(MobileUserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto);

    //履职信息相关接口

    /**
     * 履职列表
     * @param userDetails
     * @return
     */
    RespBody performancePage(MobileUserDetailsImpl userDetails, PerformancePageDto performancePageDto);


    /**
     * 履职类型列表
     * @param viewDto
     * @return
     */
    RespBody performanceDetail(ViewDto viewDto);

    /**
     * 添加或修改履职
     * @param userDetails
     * @return
     */
    RespBody addOrUpdatePerformance(MobileUserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto);

    /**
     * 将审核通过的建议添加到履职
     * @param userDetails
     * @return
     */
    RespBody addPerformanceFormSug(MobileUserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto, Set<SuggestionImage> suggestionImages);


    /**
     * 删除履职信息
     * @param uid
     * @return
     */
    RespBody deletePerformance(String uid);

    //履职审核相关接口

    /**
     * 该我审核的履职列表
     * @param userDetails
     * @return
     */
    RespBody performanceAuditorPage(MobileUserDetailsImpl userDetails, PerformancePageDto performancePageDto);

    /**
     * 审核履职
     * @param userDetails
     * @return
     */
    RespBody auditPerformance(MobileUserDetailsImpl userDetails, AuditPerformanceDto auditPerformanceDto);

    /**
     * 代表提交的履职列表
     * @param uidDto
     * @return
     */
    RespBody performanceList(UidDto uidDto);

}
