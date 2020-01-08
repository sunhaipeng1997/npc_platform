package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.PerformanceDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeAddDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeDto;
import com.cdkhd.npc.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PerformanceService {

    //履职类型相关接口

    /**
     * 条件查询履职类型
     * @param userDetails
     * @param performanceTypeDto
     * @return
     */
    RespBody findPerformanceType(UserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto);

    /**
     * 添加、修改履职类型
     * @param userDetails
     * @param performanceTypeAddDto
     * @return
     */
    RespBody addOrUpdatePerformanceType(UserDetailsImpl userDetails, PerformanceTypeAddDto performanceTypeAddDto);

    /**
     * 删除履职类型
     * @param uid
     * @return
     */
    RespBody deletePerformanceType(String uid);

    /**
     * 修改类型排序
     * @param uid
     * @param type
     * @return
     */
    RespBody changeTypeSequence(String uid, Byte type);

    /**
     * 修改类型状态
     * @param uid
     * @param status
     * @return
     */
    RespBody changeTypeStatus(String uid, Byte status);

    /**
     * 履职类型下拉
     * @param userDetails
     * @return
     */
    RespBody performanceTypeList(UserDetailsImpl userDetails);


    //履职相关接口

    /**
     * 条件查询履职信息
     * @param userDetails
     * @param performanceDto
     * @return
     */
    RespBody findPerformance(UserDetailsImpl userDetails, PerformanceDto performanceDto);


    /**
     * 删除履职信息
     * @param uid
     * @return
     */
    RespBody deletePerformance(String uid);

    /**
     * 导出履职信息
     * @param userDetails
     * @param performanceDto
     * @return
     */
    void exportPerformance(UserDetailsImpl userDetails, PerformanceDto performanceDto, HttpServletRequest req, HttpServletResponse res);

}
