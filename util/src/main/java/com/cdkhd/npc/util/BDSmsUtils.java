package com.cdkhd.npc.util;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.sms.SmsClient;
import com.baidubce.services.sms.SmsClientConfiguration;
import com.baidubce.services.sms.model.SendMessageV3Request;
import com.baidubce.services.sms.model.SendMessageV3Response;

import java.util.HashMap;
import java.util.Map;

public class BDSmsUtils {

    /**
     * @param phoneNumber     接收消息的电话号码
     * @param accessKeyId
     * @param accessKeySecret
     * @param code            验证码
     * @param endPoint        SME服务域名
     * @param invokeId        消息签名id
     * @param templateCode    消息模板id
     * @param timeout    消息超时分钟数
     * @return
     */
    public static SendMessageV3Response sendSms(String phoneNumber, String accessKeyId, String accessKeySecret, int code, String endPoint, String invokeId, String templateCode, int timeout) {
        // ak、sk等config
        SmsClientConfiguration config = new SmsClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKeyId, accessKeySecret));
        config.setEndpoint(endPoint);

        // 实例化发送客户端
        SmsClient smsClient = new SmsClient(config);

        // 定义请求参数
        Map<String, String> vars = new HashMap<String, String>(); // 若模板内容为：您的验证码是${code},在${time}分钟内输入有效
        vars.put("code", String.valueOf(code));
        vars.put("minute", String.valueOf(timeout));

        //验证码
        System.out.println("code:        " + code);
        // 发送请求
        SendMessageV3Request request = new SendMessageV3Request();
        request.setMobile(phoneNumber);
        request.setSignatureId(invokeId);
        request.setTemplate(templateCode);
        request.setContentVar(vars);
        SendMessageV3Response response = smsClient.sendMessage(request);
        // 解析请求响应 response.isSuccess()为true 表示成功
        if (response != null && response.isSuccess()) {
            //  submit success
            System.out.println("发送成功！");
        } else {
            // fail
            System.out.println("发送失败！" + response.getMessage());
        }
        return response;
    }
}
