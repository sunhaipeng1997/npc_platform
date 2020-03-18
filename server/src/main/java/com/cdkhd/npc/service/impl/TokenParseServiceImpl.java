package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.repository.base.LoginUPRepository;
import com.cdkhd.npc.service.TokenParseService;
import com.cdkhd.npc.util.JwtUtils;
import com.cdkhd.npc.vo.RespBody;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Map;

@Service
public class TokenParseServiceImpl implements TokenParseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OncePerRequestFilter.class);

    private LoginUPRepository loginUPRepository;

    @Autowired
    public TokenParseServiceImpl(LoginUPRepository loginUPRepository) {
        this.loginUPRepository = loginUPRepository;
    }

    @Override
    public RespBody parseToken(String token) {
        RespBody body = new RespBody();
        if (StringUtils.isNotBlank(token)) {
            try {
                //验证token并解析用户信息
                Map<String, Object> userInfo = JwtUtils.parseJwt(token);
                body.setData(userInfo);
            } catch (ExpiredJwtException e) {
                LOGGER.warn("token已过期，请重新登录");
                e.printStackTrace();
                body.setMessage("token已过期，请重新登录");
                body.setStatus(HttpStatus.FORBIDDEN);
            } catch (Exception e) {
                LOGGER.warn("token解析失败");
                e.printStackTrace();
                body.setMessage("token解析失败");
                body.setStatus(HttpStatus.BAD_REQUEST);
            }finally {
                return body;
            }
        }
        return body;
    }
}
