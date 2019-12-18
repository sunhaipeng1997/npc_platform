package com.cdkhd.npc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house/system")
public class SystemController {

    @GetMapping
    public ResponseEntity getSystemList() {
        System.out.println(11111);
        return ResponseEntity.ok(1);
    }

}
