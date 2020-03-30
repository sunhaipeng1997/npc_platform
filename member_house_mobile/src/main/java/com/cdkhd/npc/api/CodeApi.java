package com.cdkhd.npc.api;

import com.cdkhd.npc.service.CodeService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/code")
public class CodeApi {

    private CodeService codeService;

    @Autowired
    public CodeApi(CodeService codeService) {
        this.codeService = codeService;
    }


    /**
     * 小程序发送验证码
     * @param mobile
     * @return
     */
    @PostMapping("/sendCode")
    public ResponseEntity sendCode(String mobile) {
        RespBody body = codeService.sendCode(mobile);
        return ResponseEntity.ok(body);
    }
}
