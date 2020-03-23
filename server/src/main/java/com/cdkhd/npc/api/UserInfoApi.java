package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.service.AccountService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/mobile/info")
public class UserInfoApi {

    private AccountService accountService;

    @Autowired
    public UserInfoApi(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 获取当前用户的菜单
     * @param userDetails
     * @return
     */
    @GetMapping("/getMyInfo")
    public ResponseEntity getMyInfo(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = accountService.getMyInfo(userDetails);
        return ResponseEntity.ok(body);
    }

}
