package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.service.SessionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/manager/session")
public class SessionController {

    private SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * 获取届期列表
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @GetMapping("/sessions")
    public ResponseEntity getSessions(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = sessionService.getSessions(userDetails);
        return ResponseEntity.ok(body);
    }

}
