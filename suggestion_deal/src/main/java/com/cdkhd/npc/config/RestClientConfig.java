package com.cdkhd.npc.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {
    //配置一个RestTemplate Bean，Spring Boot 1.4以后不会自动生成一个该类对象
    //微信服务器请求相关接口配置
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
//        return new RestTemplate();
    }

    //手动配置一个PasswordEncoder对明文密码做hash和验证
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
