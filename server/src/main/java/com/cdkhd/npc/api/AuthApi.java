package com.cdkhd.npc.api;

import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.vo.RespBody;
import com.cdkhd.npc.vo.TokenVo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(value = "认证接口")
@Controller
@RequestMapping("/api/mobile/auth")
public class AuthApi {

    private final AuthService authService;

    @Autowired
    public AuthApi(AuthService authService) {
        this.authService = authService;
    }

    @ApiOperation(
            value = "获取token",
            httpMethod = "GET",
            notes = "小程序认证比较特殊，只需要传入小程序登录时用的临时 code",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    paramType = "query",
                    dataType = "String",
                    name = "code",
                    value = "小程序登录的临时code",
                    example = "0613D8p72Pr9EJ0BUYl72NSXo723D8pZ",
                    required = true
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取token成功", response = TokenVo.class)
    })
    @GetMapping("/token")
    public ResponseEntity auth(String nickName, String code,String encryptedData, String iv) {
        RespBody body = authService.auth(nickName, code,encryptedData,iv);
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "获取服务号用户的access_token")
    @GetMapping("/access_token")
    public String accessToken(String code, String state) {
        return authService.accessToken(code, state);
    }


}
