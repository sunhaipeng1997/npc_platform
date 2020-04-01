package com.cdkhd.npc.api;

import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/mobile/auth")
public class AuthApi {

    private final AuthService authService;

    @Autowired
    public AuthApi(AuthService authService) {
        this.authService = authService;
    }


    @GetMapping("/token")
    public ResponseEntity auth(String nickName, String code, String encryptedData, String iv) {
        RespBody body = authService.auth(nickName, code, encryptedData, iv);
        return ResponseEntity.ok(body);
    }

    /*
     * 获取服务号用户的access_token
     */

    @GetMapping("/access_token")
    public String accessToken(String code, String state) {
        return authService.accessToken(code, state);
    }

}
