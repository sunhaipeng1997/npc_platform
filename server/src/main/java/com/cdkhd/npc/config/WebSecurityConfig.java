package com.cdkhd.npc.config;

import com.cdkhd.npc.component.TokenAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //使用token验证，禁止跨域
                .csrf().disable()

                //token访问，关闭session。不使用session保存SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .authorizeRequests()
                //登录接口权限允许
                .antMatchers("/api/manager/auth/login",
                        "/api/manager/auth/code",
                        "/api/manager/token/parseToken",
                        "/api/manager/member/avatar",
                        "/api/manager/upload/**",
                        "/api/mobile/auth/**",
                        "/api/mobile/push",
                        "/api/mobile/system/getSystemList",
                        "/api/manager/news/upload_image",
                        "/api/manager/study/uploadStudyFile",
                        "/api/manager/notification/upload_attachment",
                        "/api/mobile/basic_services/**",
                        "/api/mobile/news/**",
                        "/api/mobile/study/**",
                        "/api/manager/auth/thirdLogin",
                        "/public/**"
                        )
                .permitAll()
                .antMatchers(HttpMethod.OPTIONS)
                .permitAll()

                //其余接口访问受限
                .anyRequest()
                .authenticated();   //需要验证
//                .permitAll();   //暂时放行

        // 禁用缓存
        http.headers().cacheControl();

        // 添加token过滤器
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        //添加自定义未登录和未授权结果返回
        http.exceptionHandling()
                .accessDeniedHandler(restfulAccessDeniedHandler())
                .authenticationEntryPoint(restAuthenticationEntryPoint());
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    //自定义未认证的返回结果
    //AuthenticationEntryPoint 用来解决匿名用户访问无权限资源时的异常
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (req, resp, authException) -> resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }

    //自定义未授权的返回结果
    //AccessDeniedHandler 用来解决认证过的用户访问无权限资源时的异常
    @Bean
    public AccessDeniedHandler restfulAccessDeniedHandler() {
        return (req, resp, accessDeniedException) -> resp.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
    }
}
