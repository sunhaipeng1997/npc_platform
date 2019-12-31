package com.cdkhd.npc.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


/**
 * @创建人
 * @创建时间 2018/10/18
 * @描述
 */
public class Certification {

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
        System.out.println("status      :" +  respEntity.getStatusCode());
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject respBody = respEntity.getBody();
            System.out.print("respBody  :");
            System.out.println(respBody);
            if (respBody != null) {
                if ((respBody.getIntValue("errcode") == 0)) {
                    System.out.println("success");
                } else {
                    System.out.println("failed");
                }
            }
        } else {
            System.out.println("failed");
        }
    }



}
