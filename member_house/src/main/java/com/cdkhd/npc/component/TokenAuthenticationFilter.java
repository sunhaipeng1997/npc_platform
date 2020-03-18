package com.cdkhd.npc.component;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.LoginUPRepository;
import com.google.common.collect.Sets;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OncePerRequestFilter.class);

    @Autowired
    private AccountRepository accountRepository;
    private LoginUPRepository loginUPRepository;
    private RestTemplate restTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从请求中获取token
        String accessToken = getToken(request);
        //fixme url需要写在配置文件里面
        String url = "http://localhost:..../api/manager/token/parseToken";
        if (StringUtils.isNotBlank(accessToken)) {
            try {
                //验证token并解析用户信息
                Map<String, Object> userInfo = new HashMap<>();

                //设置http头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                //构造的菜单数据作为请求体
                JSONObject requestBody = new JSONObject();
                requestBody.put("token", accessToken);
                HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toJSONString(), headers);

                //调用微信服务器接口，创建公众号菜单
                ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
                JSONObject jsonObj = responseEntity.getBody();
                if (jsonObj != null && jsonObj.get("status").equals(HttpStatus.OK)){
                    userInfo = (Map<String, Object>) jsonObj.get("data");
                }else {
                    logger.info("token解析失败");
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    //保存认证信息到SecurityContext
                    //从token中解析用户的角色信息
                    List<String> roles = (List<String>) userInfo.get("accountRoles");
                    Account account =  loginUPRepository.findByUsername(userInfo.get("username").toString()).getAccount();
                    UserDetailsImpl userDetails1 = new UserDetailsImpl(account.getUid(), account.getLoginUP().getUsername(), account.getLoginUP().getPassword(), Sets.newHashSet(roles), account.getVoter().getArea(), account.getVoter().getTown(), LevelEnum.TOWN.getValue());
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails1, null, Collections.emptySet());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("合法访问，username: " + userInfo.get("username").toString());
                }
            } catch (ExpiredJwtException e) {
                logger.warn("token已过期，请重新登录");
                e.printStackTrace();
            } catch (Exception e) {
                logger.warn("token解析失败");
                e.printStackTrace();
            }
        }
        //不管token验证通过与否，继续下一个过滤
        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest httpReq) {
        //http请求头部中token的key名
        String tokenHeader = "Authorization";
        //先从http头部中获取token
        String accessToken = httpReq.getHeader(tokenHeader);
        //如果头部没有则从请求参数中获取
        if (StringUtils.isBlank(accessToken)) {
            accessToken = httpReq.getParameter(tokenHeader);
        }
        return accessToken;
    }
}
