package com.cdkhd.npc.api;

import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.service.RegisterService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/register")
public class RegisterApi {

    private RegisterService registerService;

    @Autowired
    public RegisterApi(RegisterService registerService) {
        this.registerService = registerService;
    }


    /**
     * 获取当前区、镇、村、关系
     * @return
     */
    @GetMapping("/getRelations")
    public ResponseEntity getRelations() {
        RespBody body = registerService.getRelations();
        return ResponseEntity.ok(body);
    }
    /**
     * 注册账号
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity register(UserInfoDto userInfoDto) {
        RespBody body = registerService.register(userInfoDto);
        return ResponseEntity.ok(body);
    }


}
