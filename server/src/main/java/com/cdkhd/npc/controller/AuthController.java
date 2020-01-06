package com.cdkhd.npc.controller;

import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/auth")
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/code")
    public ResponseEntity getCode(String username) {
        RespBody body = authService.getCode(username);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity login(UsernamePasswordDto upDto) {
        RespBody body = authService.login(upDto);
        return ResponseEntity.ok(body);
    }
}