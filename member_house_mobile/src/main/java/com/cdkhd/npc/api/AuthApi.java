package com.cdkhd.npc.api;

import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/auth")
public class AuthApi {

    private final AuthService authService;

    @Autowired
    public AuthApi(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/code")
    public ResponseEntity getCode(String username) {
        RespBody body = authService.getCode(username);
        return ResponseEntity.ok(body);
    }
}
