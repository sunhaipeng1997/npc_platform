package com.cdkhd.npc.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.WeChatAccessToken;
import com.cdkhd.npc.entity.WeChatMenu;
import com.cdkhd.npc.repository.base.WeChatAccessTokenRepository;
import com.cdkhd.npc.repository.base.WeChatMenuRepository;
import com.cdkhd.npc.util.SysUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * 微信服务号配置
 *
 */
@Configuration
public class WeChatServiceAccountConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatServiceAccountConfig.class);

    private final WeChatMenuRepository weChatMenuRepository;

    private final Environment env;

    private final WeChatAccessTokenRepository weChatAccessTokenRepository;

    private final RestTemplate restTemplate;

    private final String CURRENT_APPID;
    private final String CURRENT_APPSECRET;
    private final String REDIRECT_URL;

    @Autowired
    public WeChatServiceAccountConfig(WeChatMenuRepository weChatMenuRepository, Environment env, WeChatAccessTokenRepository weChatAccessTokenRepository, RestTemplate restTemplate) {
        this.weChatMenuRepository = weChatMenuRepository;
        this.env = env;
        this.weChatAccessTokenRepository = weChatAccessTokenRepository;
        this.restTemplate = restTemplate;
        CURRENT_APPID = env.getProperty("service_app.appid");
        CURRENT_APPSECRET = env.getProperty("service_app.appsecret");
        REDIRECT_URL = env.getProperty("service_app.redirect_url");
    }

    @PostConstruct
    public void init() {
        insertWeChatMenu();
    }

    private void insertWeChatMenu() {
        JSONArray array = new JSONArray();
        List<WeChatMenu> menus = new ArrayList<>();
        // 登录公众号
        String uniqueKey = "wechat_user_auth";
        WeChatMenu menu = weChatMenuRepository.findByUniqueKey(uniqueKey);
        if (menu == null) {
            menu = new WeChatMenu();
            JSONObject obj = new JSONObject();
            menu.setUniqueKey(uniqueKey);

            String name = "登录公众号";
            menu.setName(name);
            obj.put("name", name);

            String type = "view";
            menu.setType(type);
            obj.put("type", type);

            String url = String.format(
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s#wechat_redirect",
                    CURRENT_APPID,
                    REDIRECT_URL,
                    "snsapi_userinfo",
                    SysUtil.uid()

            );
            menu.setUrl(url);
            obj.put("url", url);
            menus.add(menu);
            array.add(obj);
        }

        // 跳转到小程序
        uniqueKey = "wechat_jumpto_miniprogram";
        menu = weChatMenuRepository.findByUniqueKey(uniqueKey);
        if (menu == null) {
            menu = new WeChatMenu();
            JSONObject obj = new JSONObject();
            menu.setUniqueKey(uniqueKey);

            String name = "跳转到小程序";
            menu.setName(name);
            obj.put("name", name);

            String type = "miniprogram";
            menu.setType(type);
            obj.put("type", type);

            String appid = env.getProperty("miniapp.appid");
            menu.setAppid(appid);
            obj.put("appid", appid);

            String url = env.getProperty("service_app.pagepath");
            menu.setUrl(url);
            obj.put("url", url);

            String pagepath = env.getProperty("service_app.pagepath");
            menu.setPagepath(pagepath);
            obj.put("pagepath", pagepath);

            menus.add(menu);
            array.add(obj);
        }

        if (!array.isEmpty()) {
            String appid = env.getProperty("service_app.appid");
            WeChatAccessToken token = weChatAccessTokenRepository.findByAppid(appid);
            if (token == null) {
                token = getToken();
            }
            if (token != null) {
                if (!verifyToken(token)) token = getToken();
                pushMenus(token, array, menus);
            }

        }

    }

    private void pushMenus(WeChatAccessToken token, JSONArray array, List<WeChatMenu> menus) {
        JSONObject root = new JSONObject();
        root.put("button", array);
        String postMenuUrl = String.format("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s", token.getAccessToken());
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> reqEntity = new HttpEntity<>(root.toJSONString(), headers);
        ResponseEntity<JSONObject> respEntity = restTemplate.postForEntity(postMenuUrl, reqEntity, JSONObject.class);
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject body = respEntity.getBody();
            if (body == null) return;
            if (body.getIntValue("errcode") == 0) {
                weChatMenuRepository.saveAll(menus);
                LOGGER.info("create menu success.");
                return;
            }
        }
        LOGGER.warn("create menu failed.");
    }

    @Scheduled(cron = "* * 3 * * ?")
    private void refreshToken() {
        getToken();
    }

    public WeChatAccessToken getToken() {

        WeChatAccessToken token = weChatAccessTokenRepository.findByAppid(CURRENT_APPID);
        if (token != null) {
            if (verifyToken(token)) return token;
        } else {
            token = new WeChatAccessToken();
            token.setAppid(CURRENT_APPID);
        }

        LOGGER.info("get token at: {}", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        // 构造获取access_token 的url
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", CURRENT_APPID, CURRENT_APPSECRET);
        ResponseEntity<JSONObject> respEntity = restTemplate.getForEntity(url, JSONObject.class);
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject body = respEntity.getBody();
            if (body != null) {
                token.setAccessToken(body.getString("access_token"));
                weChatAccessTokenRepository.saveAndFlush(token);
                return token;
            }
        }

        return null;
    }

    public boolean verifyToken(WeChatAccessToken token) {
        if (token == null) return false;
        String url = String.format("https://api.weixin.qq.com/cgi-bin/menu/get?access_token=%s", token.getAccessToken());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> reqEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<JSONObject> respEntity = restTemplate.exchange(url, HttpMethod.GET, reqEntity, JSONObject.class);
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject body = respEntity.getBody();
            if (body != null) {
                if (body.getIntValue("errcode") == 0) {
                    return true;
                }
            }
        }
        return false;
    }

}
