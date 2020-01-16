package com.cdkhd.npc.util;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


/**
 * @创建人
 * @创建时间 2018/10/18
 * @描述
 */
public class Certification {
    private static final Logger LOGGER = LoggerFactory.getLogger(Certification.class);

    public static void send(String token, String body) {

        //封装推送消息url
        String sendUrl = String.format(
                "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s",
                token
        );
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> reqEntity = new HttpEntity<>(body, headers);
        ResponseEntity<JSONObject> respEntity = restTemplate.postForEntity(sendUrl, reqEntity, JSONObject.class);
        LOGGER.error(respEntity.getStatusCode().toString());
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject respBody = respEntity.getBody();
            LOGGER.error(respBody.toString());
            if ((respBody.getIntValue("errcode") == 0)) {
                LOGGER.info("消息发送成功!");
            } else {
                LOGGER.error("消息发送失败!");
            }
        } else {
            LOGGER.error("消息发送失败!");
        }
    }



}
