package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.UnitAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitPageDto;
import com.cdkhd.npc.entity.dto.UnitUserAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitUserPageDto;
import com.cdkhd.npc.service.UnitService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/unit_manage")
public class UnitController {

    private UnitService unitService;

    @Autowired
    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    /**
     * 分页查询单位信息
     */
    @GetMapping("/unitPage")
    public ResponseEntity unitPage(@CurrentUser UserDetailsImpl userDetails, UnitPageDto unitPageDto) {
        RespBody body = unitService.unitPage(userDetails, unitPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加或修改单位
     */
    @PostMapping("/addOrUpdateUnit")
    public ResponseEntity addOrUpdateUnit(@CurrentUser UserDetailsImpl userDetails, UnitAddOrUpdateDto unitAddOrUpdateDto) {
        RespBody body = unitService.addOrUpdateUnit(userDetails, unitAddOrUpdateDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 单位详细信息
     */
    @GetMapping("/unitDetails")
    public ResponseEntity unitDetails(BaseDto baseDto) {
        RespBody body = unitService.unitDetails(baseDto.getUid());
        return ResponseEntity.ok(body);
    }


    /**
     * 删除单位信息
     */
    @DeleteMapping("/deleteUnit")
    public ResponseEntity deleteUnit(BaseDto baseDto) {
        RespBody body = unitService.deleteUnit(baseDto.getUid());
        return ResponseEntity.ok(body);
    }


    /**
     * 修改单位状态
     */
    @PostMapping("/changeUnitStatus")
    public ResponseEntity changeUnitStatus(String uid, Byte status) {
        RespBody body = unitService.changeUnitStatus(uid, status);
        return ResponseEntity.ok(body);
    }


    /**
     * 获取单位下拉列表
     */
    @GetMapping("/unitList")
    public ResponseEntity unitList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = unitService.unitList(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 根据类型获取单位下拉列表
     */
    @GetMapping("/unitListByType")
    public ResponseEntity unitListByType(BaseDto baseDto) {
        RespBody body = unitService.unitListByType(baseDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 分页查询单位人员信息
     */
    @GetMapping("/unitUserPage")
    public ResponseEntity unitUserPage(@CurrentUser UserDetailsImpl userDetails, UnitUserPageDto unitUserPageDto) {
        RespBody body = unitService.unitUserPage(userDetails, unitUserPageDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 添加或修改单位人员信息
     */
    @PostMapping("/addOrUpdateUnitUser")
    public ResponseEntity addOrUpdateUnitUser(@CurrentUser UserDetailsImpl userDetails, UnitUserAddOrUpdateDto unitUserAddOrUpdateDto) {
        RespBody body = unitService.addOrUpdateUnitUser(userDetails, unitUserAddOrUpdateDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 单位人员详细信息
     */
    @GetMapping("/unitUserDetails")
    public ResponseEntity unitUserDetails(BaseDto baseDto) {
        RespBody body = unitService.unitUserDetails(baseDto.getUid());
        return ResponseEntity.ok(body);
    }



    /**
     * 单位人员信息
     */
    @DeleteMapping("/deleteUnitUser")
    public ResponseEntity deleteUnitUser(BaseDto baseDto) {
        RespBody body = unitService.deleteUnitUser(baseDto.getUid());
        return ResponseEntity.ok(body);
    }

    /**
     * 单位人员信息
     */
    @DeleteMapping("/resetPwd")
    public ResponseEntity resetPwd(BaseDto baseDto) {
        RespBody body = unitService.resetPwd(baseDto.getUid());
        return ResponseEntity.ok(body);
    }
}
