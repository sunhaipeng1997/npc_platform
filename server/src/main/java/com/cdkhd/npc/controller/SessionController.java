package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.entity.dto.SessionAddDto;
import com.cdkhd.npc.entity.dto.SessionPageDto;
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

    /**
     * 分页查询届期信息
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @GetMapping("/sessionPage")
    public ResponseEntity sessionPage(@CurrentUser UserDetailsImpl userDetails, SessionPageDto sessionPageDto) {
        RespBody body = sessionService.sessionPage(userDetails, sessionPageDto);
        return ResponseEntity.ok(body);
    }
    /**
     * 添加或修改届期信息
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @PostMapping("/addOrUpdateSession")
    public ResponseEntity addOrUpdateSession(@CurrentUser UserDetailsImpl userDetails, SessionAddDto sessionAddDto) {
        RespBody body = sessionService.addOrUpdateSession(userDetails, sessionAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @DeleteMapping("/deleteSessions")
    public ResponseEntity deleteSessions(@CurrentUser UserDetailsImpl userDetails, String uid) {
        RespBody body = sessionService.deleteSessions(userDetails, uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 换届
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @PostMapping("/clearSessions")
    public ResponseEntity clearSessions(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = sessionService.clearSessions(userDetails);
        return ResponseEntity.ok(body);
    }


    /**
     * 获取本届uid
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @GetMapping("/getCurrentSession")
    public ResponseEntity getCurrentSession(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = sessionService.getCurrentSession(userDetails);
        return ResponseEntity.ok(body);
    }
}
