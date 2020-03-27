package com.cdkhd.npc.component;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.LoginUPRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.google.common.collect.Sets;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    private static final Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

//    @Autowired
//    private LoginUPRepository loginUPRepository;
//    @Autowired
//    private RestTemplate restTemplate;
//    @Autowired
//    private Environment environment;
    @Autowired
    private TownRepository townRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从请求中获取token
//        String accessToken = getToken(request);
//        String url = "http://127.0.0.1:8080/" + accessToken;
//        String url = environment.getProperty("serverUrl");
//        if (StringUtils.isNotBlank(accessToken)) {
//            try {
                //验证token并解析用户信息
//                Map<String, Object> userInfo = new HashMap<>();
//
                //设置http头
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
                //构造的参数作为请求体
//                JSONObject requestBody = new JSONObject();
//                requestBody.put("token", accessToken);
//                HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toJSONString(), headers);
//
                //调用server接口，获取解析token后的用户信息
//                ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET , httpEntity, JSONObject.class);
//                JSONObject jsonObj = responseEntity.getBody();

//                String str = jsonObj.get("status").toString();
//
//                if (jsonObj != null && jsonObj.get("status").toString().equals(HttpStatus.OK.name())){
//                    userInfo = (Map<String, Object>) jsonObj.get("data");
//                }else {
//                    logger.info("token解析失败");
//                }

//                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    //保存认证信息到SecurityContext
                    //从token中解析用户的角色信息
//                    List<String> roles = (List<String>) userInfo.get("accountRoles");
//                    Account account =  loginUPRepository.findByUsername(userInfo.get("username").toString()).getAccount();
        Town town = townRepository.findByUid("ce9028c82dd811ea8f3f0242ac170005");
                    UserDetailsImpl userDetails1 = new UserDetailsImpl("751806ea2d4211ea8f3f0242ac170005", "admin", "123456", Sets.newHashSet("NPC_MEMBER"), town.getArea(), town, LevelEnum.AREA.getValue());
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails1, null, Collections.emptySet());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
//                    logger.info("合法访问，username: " + userInfo.get("username").toString());
//                }
//            } catch (ExpiredJwtException e) {
//                logger.warn("token已过期，请重新登录");
//                e.printStackTrace();
//            } catch (Exception e) {
//                logger.warn("token解析失败");
//                e.printStackTrace();
//            }
//        }
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
