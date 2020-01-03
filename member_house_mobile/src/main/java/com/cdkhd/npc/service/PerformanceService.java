package com.cdkhd.npc.service;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.vo.RespBody;

public interface PerformanceService {

    //履职类型相关接口

    /**
     * 履职类型列表
     * @param userDetails
     * @return
     */
    RespBody performanceTypeList(UserDetailsImpl userDetails);

    //履职信息相关接口

    /**
     * 添加或修改履职
     * @param userDetails
     * @return
     */
    RespBody addOrUpdatePerformance(@CurrentUser UserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto);





}
