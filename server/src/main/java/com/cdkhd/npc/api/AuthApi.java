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


    /**
     * 小程序用户登录授权
     * @param nickName
     * @param code
     * @param encryptedData
     * @param iv
     * @return
     */
    @GetMapping("/token")
    public ResponseEntity auth(String nickName, String code, String encryptedData, String iv) {
        RespBody body = authService.auth(nickName, code, encryptedData, iv);
        return ResponseEntity.ok(body);
    }

    /**
     * 服务号用户登录授权（即服务号菜单项 “登录公众号” 跳转接口）
     * @param code 用户 code
     * @param state 自定义参数
     * @return 授权成功/失败界面（使用 thymeleaf 模板引擎）
     */
    @GetMapping("/access_token")
    public String accessToken(String code, String state) {
        return authService.accessToken(code, state);
    }
}
