package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.service.SystemService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/system")
public class SystemController {
    private SystemService systemService;

    @Autowired
    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }


    //获取能够进入的系统列表
    @GetMapping("/getSystemList")
    public ResponseEntity getSystemList() {
        RespBody body = systemService.getSystemList();
        return ResponseEntity.ok(body);
    }

    //获取我上次登录缓存的系统
    @GetMapping("/getCacheSystem")
    public ResponseEntity getCacheSystem(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = systemService.getCacheSystem(userDetails.getUid());
        return ResponseEntity.ok(body);
    }

    //缓存本次选择的系统
    @PostMapping("/cacheSystem")
    public ResponseEntity cacheSystem(@CurrentUser UserDetailsImpl userDetails, String systemId) {
        RespBody body = systemService.cacheSystem(userDetails.getUid(),systemId);
        return ResponseEntity.ok(body);
    }
}
