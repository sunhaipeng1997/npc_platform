package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/member")
public class NpcMemberController {

    @GetMapping("/page")
    public ResponseEntity pageOfNpcMember(@CurrentUser UserDetailsImpl userDetails, NpcMemberPageDto pageDto) {
        return ResponseEntity.ok("");
    }
}
