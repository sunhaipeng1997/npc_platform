package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.WorkStationAddDto;
import com.cdkhd.npc.entity.dto.WorkStationPageDto;
import com.cdkhd.npc.service.WorkStationService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/member_house/work_stations")
public class WorkStationController {

    private final WorkStationService workStationService;

    @Autowired
    public WorkStationController(WorkStationService workStationService) {
        this.workStationService = workStationService;
    }

    /**
     * 分页查询镇或者区的工作站
     * */
    @GetMapping("/page")
    public ResponseEntity page(@CurrentUser UserDetailsImpl userDetails, WorkStationPageDto workStationPageDto){
        RespBody body = workStationService.page(userDetails, workStationPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 上传文件
     * */
    @PostMapping("/uploadWorkStationAvatar")
    public ResponseEntity uploadWorkStationAvatar(@CurrentUser UserDetailsImpl userDetails, MultipartFile file){
        RespBody body = workStationService.upload(userDetails, file);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加和修改工作站
     * */
    @PostMapping("/addOrUpdate")
    public ResponseEntity addOrUpdate(@CurrentUser UserDetailsImpl userDetails, WorkStationAddDto workStationAddDto){
        RespBody body = workStationService.addOrUpdate(userDetails, workStationAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 改变工作站启用、禁用状态
     * */
    @PostMapping("/changeStatus")
    public ResponseEntity changeStatus(String uid){
        RespBody body = workStationService.changeStatus(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除工作站
     * */
    @DeleteMapping("/delete")
    public ResponseEntity delete(String uid){
        RespBody body = workStationService.delete(uid);
        return ResponseEntity.ok(body);
    }

}
