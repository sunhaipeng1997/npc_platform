package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.PerformanceDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeAddDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeDto;
import com.cdkhd.npc.vo.RespBody;

public interface PerformanceService {

    //履职类型相关接口
    RespBody findPerformanceType(UserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto);

    RespBody addOrUpdatePerformanceType(UserDetailsImpl userDetails, PerformanceTypeAddDto performanceTypeAddDto);

    RespBody deletePerformanceType(String uid);

    RespBody changeTypeSequence(String uid, Byte type);

    RespBody changeTypeStatus(String uid, Byte status);

    //履职相关接口

    RespBody findPerformance(UserDetailsImpl userDetails, PerformanceDto performanceDto);

}
