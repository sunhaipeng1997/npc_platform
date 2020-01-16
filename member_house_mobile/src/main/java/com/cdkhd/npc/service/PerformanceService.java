package com.cdkhd.npc.service;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.PerformanceType;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.entity.dto.AuditPerformanceDto;
import com.cdkhd.npc.entity.dto.PerformancePageDto;
import com.cdkhd.npc.vo.RespBody;

public interface PerformanceService {

    //履职类型相关接口

    /**
     * 履职类型列表
     * @param userDetails
     * @return
     */
    RespBody performanceTypeList(UserDetailsImpl userDetails, PerformanceType performanceType);

    //履职信息相关接口

    /**
     * 履职列表
     * @param userDetails
     * @return
     */
    RespBody performancePage(UserDetailsImpl userDetails, PerformancePageDto performancePageDto);


    /**
     * 履职类型列表
     * @param uid
     * @return
     */
    RespBody performanceDetail(String uid);

    /**
     * 添加或修改履职
     * @param userDetails
     * @return
     */
    RespBody addOrUpdatePerformance(@CurrentUser UserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto);

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
    RespBody performanceAuditorPage(UserDetailsImpl userDetails, PerformancePageDto performancePageDto);

    /**
     * 审核履职
     * @param userDetails
     * @return
     */
    RespBody auditPerformance(UserDetailsImpl userDetails, AuditPerformanceDto auditPerformanceDto);

}
