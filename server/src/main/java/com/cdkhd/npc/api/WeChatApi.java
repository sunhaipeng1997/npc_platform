package com.cdkhd.npc.api;

import com.cdkhd.npc.service.WeChatService;
import com.cdkhd.npc.util.WeChatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/api/mobile/login")
public class WeChatApi {

    private WeChatService weChatService;

    @Autowired
    public WeChatApi(WeChatService weChatService) {
        this.weChatService = weChatService;
    }

    /**
     * 接受微信服务器的验证请求，完成微信公众平台开发者认证
     * @param signature 待比对的签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 随机字符串
     * @return 验证成功返回echostr
     */
    @GetMapping("/wx")
    public ResponseEntity checkWeChatServer(String signature, String timestamp, String nonce, String echostr) {
        if (StringUtils.isEmpty(signature) || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(nonce) || StringUtils.isEmpty(echostr)) {
            return ResponseEntity.ok("");
        }

        //需要与公众号设置中填入的token一致（微信公众平台->开发（基本配置）->服务器配置->令牌（token））
        String token = "ImCDKHD";

        if (WeChatUtils.checkSign(signature, timestamp, nonce, token)) {
            return ResponseEntity.ok(echostr);
        } else {
            return ResponseEntity.ok("");
        }
    }

    /**
     * 获取配置参数，用于前端页面调用js api获取微信的扫码等功能
     * @param url 要使用js api的前端页面的url
     * @return 配置参数
     */
    @GetMapping("/config")
    public ResponseEntity getConfig4JsApi(String url) {
        Map<String, String> config = weChatService.getConfig4JsApi(url);
        return ResponseEntity.ok(config);
    }
}
