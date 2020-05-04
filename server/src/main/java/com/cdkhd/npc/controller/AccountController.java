package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AccountPageDto;
import com.cdkhd.npc.service.AccountService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/account")
public class AccountController {

    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 查询账号相关信息
     *
     * @return
     */
    @GetMapping("/findAccount")
    public ResponseEntity findAccount(@CurrentUser UserDetailsImpl userDetails, AccountPageDto accountPageDto){
        RespBody body = accountService.findAccount(userDetails, accountPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改账号状态
     *
     * @return
     */
    @PostMapping("/changeStatus")
    public ResponseEntity changeStatus(String uid, Byte status){
        RespBody body = accountService.changeStatus(uid, status);
        return ResponseEntity.ok(body);
    }
}
