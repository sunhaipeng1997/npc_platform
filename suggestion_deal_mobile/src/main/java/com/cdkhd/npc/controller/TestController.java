package com.cdkhd.npc._platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @GetMapping("/hello")
    public @ResponseBody String hello() {
        return "hello, world";
    }

    @GetMapping("/index")
    public @ResponseBody String index() {
        return "index";
    }
}
