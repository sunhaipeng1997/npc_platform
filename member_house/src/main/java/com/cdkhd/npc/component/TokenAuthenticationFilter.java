package com.cdkhd.npc.component;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.util.JwtUtils;
import com.google.common.collect.Sets;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OncePerRequestFilter.class);

    @Autowired
    private AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从请求中获取token
        String accessToken = getToken(request);
        if (StringUtils.isNotBlank(accessToken)) {
            try {
                //验证token并解析用户信息
                Map<String, Object> userInfo = JwtUtils.parseJwt(accessToken);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    //保存认证信息到SecurityContext
                    //从token中解析用户的角色信息
                    List<String> roles = (List<String>)userInfo.get("accountRoles");
                    List<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    Account account = accountRepository.findByLoginUPUsername(userInfo.get("username").toString());
                    UserDetailsImpl userDetails1 = new UserDetailsImpl(account.getUid(), account.getLoginUP().getUsername(), account.getLoginUP().getPassword(), Sets.newHashSet(roles), account.getVoter().getArea(), account.getVoter().getTown(), LevelEnum.TOWN.getValue());
                    UserDetails userDetails = new User(userInfo.get("username").toString(), "", authorities);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails1, null, Collections.emptySet());
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.info("合法访问，username: " + userInfo.get("username").toString());

                }
            } catch (ExpiredJwtException e) {
                logger.warn("token已过期，请重新登录");
                e.printStackTrace();
//            } catch (SignatureException e) {
//                logger.warn("签名验证失败，token不合法，拒绝访问");
//                e.printStackTrace();
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