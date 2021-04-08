package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.service.AccountService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
     * 获取当前用户的个人信息
     * @param userDetails
     * @return
     */
    @GetMapping("/getMyInfo")
    public ResponseEntity getMyInfo(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = accountService.getMyInfo(userDetails.getUid());
        return ResponseEntity.ok(body);
    }

    /**
     * 修改个人信息
     * @return
     */
    @PostMapping("/updateInfo")
    public ResponseEntity updateInfo(@CurrentUser UserDetailsImpl userDetails, UserInfoDto userInfoDto) {
        RespBody body = accountService.updateInfo(userDetails,userInfoDto);
        return ResponseEntity.ok(body);
    }

}
