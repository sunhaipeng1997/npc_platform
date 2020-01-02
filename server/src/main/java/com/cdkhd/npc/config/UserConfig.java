package com.cdkhd.npc.config;

import com.cdkhd.npc.component.UserDetailsImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class UserConfig {

    //开发阶段模拟一个UserDetailsImpl对象
    @Bean
    public UserDetailsImpl npcUser() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_NPC");

        return new UserDetailsImpl("751806ea2d4211ea8f3f0242ac170005", "ll", "password", roles);
    }
}
