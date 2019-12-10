package com.cdkhd.npc.util;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.sms.SmsClient;
import com.baidubce.services.sms.SmsClientConfiguration;
import com.baidubce.services.sms.model.SendMessageV2Request;
import com.baidubce.services.sms.model.SendMessageV2Response;

import java.util.HashMap;
import java.util.Map;

public class BDSmsUtils {

    /**
     *
     * @param phoneNumber  接收消息的电话号码
     * @param accessKeyId
     * @param accessKeySecret
     * @param code  验证码
     * @param endPoint  SME服务域名
     * @param invokeId  消息签名id
     * @param templateCode  消息模板id
     * @return
     */
    public static SendMessageV2Response sendSms(String phoneNumber,String accessKeyId,String accessKeySecret,int code,String endPoint,String invokeId,String templateCode){
        // 相关参数定义
//        String endPoint = "http://sms.bj.baidubce.com"; // SMS服务域名，可根据环境选择具体域名
//        String accessKeyId = env.getProperty("code.accessKeyId");
//        String secretAccessKy = env.getProperty("code.AccessKeySecret");
//        String accessKeyId = "u23487324298ewuroiew";  // 发送账号安全认证的Access Key ID
//        String secretAccessKy = "8273dsjhfkjdshf78327jkj"; // 发送账号安全认证的Secret Access Key

        // ak、sk等config
        SmsClientConfiguration config = new SmsClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKeyId, accessKeySecret));
        config.setEndpoint(endPoint);

        // 实例化发送客户端
        SmsClient smsClient = new SmsClient(config);

        // 定义请求参数
//        String invokeId = "0FaecrUS-V0hG-RqZG"; // 发送使用签名的调用ID
//        String phoneNumber = ""; // 要发送的手机号码(只能填写一个手机号)
//        String templateCode = "smsTpl:bfcc8c6c-f109-4cea-8a65-3fde507e85b8"; // 本次发送使用的模板Code
        Map<String, String> vars = new HashMap<String, String>(); // 若模板内容为：您的验证码是${code},在${time}分钟内输入有效
        vars.put("code", String.valueOf(code));
//        vars.put("time", "5");

        //验证码
        System.out.println("code:        " + code);
        //实例化请求对象
        SendMessageV2Request request = new SendMessageV2Request();
        request.withInvokeId(invokeId)
                .withPhoneNumber(phoneNumber)
                .withTemplateCode(templateCode)
                .withContentVar(vars);

        // 发送请求
        SendMessageV2Response response = smsClient.sendMessage(request);

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
