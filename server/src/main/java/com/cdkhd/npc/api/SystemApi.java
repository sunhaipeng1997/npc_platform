package com.cdkhd.npc.api;

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
@RequestMapping("/api/mobile/system")
public class SystemApi {
    private SystemService systemService;

    @Autowired
    public SystemApi(SystemService systemService) {
        this.systemService = systemService;
    }


    //获取能够进入的系统列表
    @GetMapping("/getSystemList")
    public ResponseEntity getSystemList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = systemService.getSystemList(userDetails);
        return ResponseEntity.ok(body);
    }

    //获取我上次登录缓存的系统
    @GetMapping("/getCacheSystem")
    public ResponseEntity getCacheSystem(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = systemService.getCacheSystem(userDetails.getUid(),(byte)2);
        return ResponseEntity.ok(body);
    }

    //缓存本次选择的系统
    @PostMapping("/cacheSystem")
    public ResponseEntity cacheSystem(@CurrentUser UserDetailsImpl userDetails, String systemId) {
        RespBody body = systemService.cacheSystem(userDetails.getUid(),systemId);
        return ResponseEntity.ok(body);
    }
}
