package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.PerformanceDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeAddDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeDto;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/member_house/performance")
public class PerformanceController {

    private PerformanceService performanceService;

    @Autowired
    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    /**
     * 获取履职类型列表
     *
     * @return
     */
    @GetMapping("/performanceType")
    public ResponseEntity performanceType(@CurrentUser UserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto) {
        RespBody body = performanceService.findPerformanceType(userDetails, performanceTypeDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加、修改履职类型
     *
     * @return
     */
    @PostMapping("/addOrUpdatePerformanceType")
    public ResponseEntity addOrUpdatePerformanceType(@CurrentUser UserDetailsImpl userDetails, PerformanceTypeAddDto performanceTypeAddDto) {
        RespBody body = performanceService.addOrUpdatePerformanceType(userDetails, performanceTypeAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除履职类型
     *
     * @return
     */
    @DeleteMapping("/deletePerformanceType")
    public ResponseEntity deletePerformanceType(String uid) {
        RespBody body = performanceService.deletePerformanceType(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改类型排序
     *
     * @return
     */
    @PostMapping("/changeTypeSequence")
    public ResponseEntity changeTypeSequence(String uid, Byte type) {
        RespBody body = performanceService.changeTypeSequence(uid, type);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改类型状态
     *
     * @return
     */
    @PostMapping("/changeTypeStatus")
    public ResponseEntity changeTypeStatus(String uid, Byte status) {
        RespBody body = performanceService.changeTypeStatus(uid, status);
        return ResponseEntity.ok(body);
    }

    /**
     * 类型下拉
     *
     * @return
     */
    @GetMapping("/performanceTypeList")
    public ResponseEntity performanceTypeList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = performanceService.performanceTypeList(userDetails);
        return ResponseEntity.ok(body);
    }

    //履职相关接口

    /**
     * 获取履职信息列表
     *
     * @return
     */
    @GetMapping("/findPerformance")
    public ResponseEntity findPerformance(@CurrentUser UserDetailsImpl userDetails, PerformanceDto performanceDto) {
        RespBody body = performanceService.findPerformance(userDetails, performanceDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除履职信息
     * @return
     */
    @DeleteMapping("/deletePerformance")
    public ResponseEntity deletePerformance(String uid) {
        RespBody body = performanceService.deletePerformance(uid);
        return ResponseEntity.ok(body);
    }


    /**
     * 导出履职信息
     *
     * @return
     */
    @PostMapping("/exportPerformance")
    public void exportPerformance(@CurrentUser UserDetailsImpl userDetails, PerformanceDto performanceDto, HttpServletRequest req, HttpServletResponse res) {
        performanceService.exportPerformance(userDetails, performanceDto, req, res);
    }
}
