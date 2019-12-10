package com.cdkhd.npc.util;

import com.cdkhd.npc.entity.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Map;

public class JwtUtils {
    //密钥，用于生成jwt签名
    private static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512); //or HS384 or HS256

    private static Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public static String createJwt(Token token) {
        String jws = Jwts.builder()
                .setIssuer("cdkhd")
                .setSubject("npc_platform")
                .setExpiration(token.getExpireAt())
                .setIssuedAt(token.getSignAt())
                .claim("username", token.getUsername())
                .claim("roles", token.getRoles())
                .signWith(key)
                .compact();

        logger.info("生成用户" + token.getUsername() + "的token：" + jws);

        return jws;
    }

    public static Map<String, Object> parseJwt(String jwsString) {
        Jws<Claims> jws = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwsString);

        return jws.getBody();
    }
}
