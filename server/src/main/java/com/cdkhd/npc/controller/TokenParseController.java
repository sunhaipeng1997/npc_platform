package com.cdkhd.npc.controller;

import com.cdkhd.npc.service.TokenParseService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/token")
public class TokenParseController {

    private TokenParseService tokenParseService;

    @Autowired
    public TokenParseController(TokenParseService tokenParseService) {
        this.tokenParseService = tokenParseService;
    }

    @GetMapping("/parseToken")
    public ResponseEntity parseToken(String token) {
        RespBody body = tokenParseService.parseToken(token);
        return ResponseEntity.ok(body);
    }

}
