package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.service.WeChatService;
import com.cdkhd.npc.util.WeChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Service
@DependsOn("restTemplate")   //该bean的初始化依赖于restTemplate，此注解保证了这两个bean的初始化顺序
public class WeChatServiceImpl implements WeChatService {
    //缓存的access token数据
    private String accessToken;
    private Date tokenExpireAt;

    //缓存的jsapi ticket数据
    private String jsApiTicket;
    private Date ticketExpireAt;

    private RestTemplate restTemplate;
    private Environment env;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatServiceImpl.class);
    private final String APPID;
    private final String APPSECRET;

    @Autowired
    public WeChatServiceImpl(RestTemplate restTemplate, Environment env) {
        this.restTemplate = restTemplate;
        this.env = env;

        APPID = env.getProperty("service_app.app_id");
        APPSECRET = env.getProperty("service_app.app_secret");
    }

    /**
     * 为微信公众号创建菜单
     */
//    @PostConstruct   //创建过菜单之后可以注释掉
    public void initMenu4OfficialAccount() {
        //创建自定义菜单接口的url
        String url = String.format("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s", getAccessToken());

        //设置http头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //构造的菜单数据作为请求体
        JSONObject requestBody = formMenuData();
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toJSONString(), headers);

        //调用微信服务器接口，创建公众号菜单
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject jsonObj = responseEntity.getBody();

        if (jsonObj == null || !jsonObj.getString("errcode").equals("0")) {
            if (jsonObj == null) {
                LOGGER.error("WeChatServiceImpl: 创建微信公众号菜单失败：无法解析响应数据");
            } else {
                LOGGER.error("创建微信公众号菜单失败: " + jsonObj.getString("errmsg"));
            }
        } else {
            LOGGER.info("创建微信公众号菜单成功");
        }
    }

    /**
     * 构造公众号菜单
     * @return json形式的菜单数据
     */
    private JSONObject formMenuData() {
        //进入商城菜单项
        JSONObject menuItem = new JSONObject();
        menuItem.put("type", "view");
        menuItem.put("name", "进入商城");
        String menuItemUrl = env.getProperty("service_app.menu_shop_url");
        menuItem.put("url", menuItemUrl);

        LOGGER.info("公众号菜单url: " + menuItemUrl);

        //公众号菜单
        JSONArray menus = new JSONArray();
        menus.add(menuItem);

        //发送给微信服务器的数据
        JSONObject data = new JSONObject();
        data.put("button", menus);

        return data;
    }

    /**
     * 生成config数据，用于前端页面调用js api接口
     * @param jsApiUrl 调用js api的前端页面的url
     * @return config数据
     */
    @Override
    public Map<String, String> getConfig4JsApi(String jsApiUrl) {
        return WeChatUtils.sign(getJsApiTicket(), jsApiUrl);
    }

    /**
     * 从微信服务器获取access token（与网页授权的access token不同）
     * @return access token
     */
    private String getAccessToken() {
        //如果access_token存在且未过期，就直接返回
        Calendar now = Calendar.getInstance();
        if (accessToken != null && now.getTime().before(tokenExpireAt)) {
            return accessToken;
        }

        //获取公众号接口access_token的url
        String getAccessTokenUrl = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                APPID,
                APPSECRET
        );
        //向微信服务器请求access_token
        ResponseEntity<JSONObject> respEntity = restTemplate.getForEntity(getAccessTokenUrl, JSONObject.class);
        JSONObject jsonObj = respEntity.getBody();

        if (jsonObj == null || jsonObj.containsKey("errcode")) {
            if (jsonObj == null) {
                LOGGER.error("WeChatServiceImpl: 从微信服务器获取access_token失败：无法解析响应数据");
            } else {
                LOGGER.error("WeChatServiceImpl: 从微信服务器获取access_token失败：" + jsonObj.getString("errmsg"));
            }
            return null;
        }

        accessToken = jsonObj.getString("access_token");
        now.add(Calendar.HOUR, 2);
        tokenExpireAt = now.getTime();

        return accessToken;
    }

    /**
     * 从微信服务器获取jsapi ticket
     * @return jsapi ticket or null
     */
    private String getJsApiTicket() {
        //如果jsapi_ticket存在且未过期，就直接返回
        Calendar now = Calendar.getInstance();
        if (jsApiTicket != null && now.getTime().before(ticketExpireAt)) {
            return jsApiTicket;
        }

        //获取jsapi_ticket的url
        String url = String.format("http://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi&access_token=%s", getAccessToken());
        //向微信服务器请求获取jsapi_ticket
        JSONObject jsonObj = restTemplate.getForObject(url, JSONObject.class);

        if (jsonObj == null || jsonObj.containsKey("errcode")) {
            if (jsonObj == null) {
                LOGGER.error("WeChatServiceImpl: 从微信服务器获取jsapi ticket失败：无法解析响应数据");
            } else {
                LOGGER.error("WeChatServiceImpl: 从微信服务器获取jsapi ticket失败：" + jsonObj.getString("errmsg"));
            }
            return null;
        }

        jsApiTicket = jsonObj.getString("jsapi_ticket");
        now.add(Calendar.HOUR, 2);
        ticketExpireAt = now.getTime();

        return jsApiTicket;
    }
}
