package com.cdkhd.npc.component;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.google.common.collect.Maps;
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

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Environment environment;

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        Town town = townRepository.findByUid("ce9028c82dd811ea8f3f0242ac170005");
//        UserDetailsImpl userDetails1 = new UserDetailsImpl("751806ea2d4211ea8f3f0242ac170005", "admin", "123456", Sets.newHashSet("NPC_MEMBER"), town.getArea(), town, LevelEnum.AREA.getValue());
//        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails1, null, Collections.emptySet());
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//        filterChain.doFilter(request, response);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //??????????????????token
        String accessToken = getToken(request);
        String url = environment.getProperty("serverUrl") + "/api/manager/token/parseToken?token={token}";
        if (StringUtils.isNotBlank(accessToken)) {
            try {
                //??????token?????????????????????
                Map<String, Object> userInfo = new HashMap<>();

                //??????http???
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                //??????????????????????????????
                JSONObject requestBody = new JSONObject();
                HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toJSONString(), headers);
                Map<String, String> map = Maps.newHashMap();
                map.put("token",accessToken);
                //??????server?????????????????????token??????????????????
                ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET , httpEntity, JSONObject.class,map);
                JSONObject jsonObj = responseEntity.getBody();
                if (jsonObj != null && jsonObj.get("status").toString().equals(HttpStatus.OK.name())){
                    userInfo = (Map<String, Object>) jsonObj.get("data");
                }else {
                    logger.info("token????????????");
                }
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    //?????????????????????SecurityContext
                    //???token??????????????????????????????
                    List<String> roles = (List<String>) userInfo.get("accountRoles");
                    Account account =  accountRepository.findByUid(userInfo.get("uid").toString());
                    MobileUserDetailsImpl userDetails = new MobileUserDetailsImpl(account.getUid(), Sets.newHashSet(roles), account.getVoter().getArea(), account.getVoter().getTown());
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptySet());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("???????????????uid: " + userInfo.get("uid").toString());
                }
            } catch (ExpiredJwtException e) {
                logger.warn("token???????????????????????????");
                e.printStackTrace();
            } catch (Exception e) {
                logger.warn("token????????????");
                e.printStackTrace();
            }
        }
        //??????token??????????????????????????????????????????
        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest httpReq) {
        //http???????????????token???key???
        String tokenHeader = "Authorization";
        //??????http???????????????token
        String accessToken = httpReq.getHeader(tokenHeader);
        //?????????????????????????????????????????????
        if (StringUtils.isBlank(accessToken)) {
            accessToken = httpReq.getParameter(tokenHeader);
        }
        return accessToken;
    }
}
