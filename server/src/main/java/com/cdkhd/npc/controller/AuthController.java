package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.PasswordDto;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/menus")
    public ResponseEntity menus(@CurrentUser UserDetailsImpl userDetails, BaseDto baseDto) {
        RespBody body = authService.menus(userDetails,baseDto);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/updatePwd")
    public ResponseEntity updatePwd(@CurrentUser UserDetailsImpl userDetails, PasswordDto passwordDto) {
        RespBody body = authService.updatePwd(userDetails,passwordDto);
        return ResponseEntity.ok(body);
    }
}
