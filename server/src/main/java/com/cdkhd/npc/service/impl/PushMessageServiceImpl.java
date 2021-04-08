package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.config.WeChatServiceAccountConfig;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.WeChatAccessToken;
import com.cdkhd.npc.enums.MsgTypeEnum;
import com.cdkhd.npc.service.PushMessageService;
import com.cdkhd.npc.util.Certification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Transactional
public class PushMessageServiceImpl implements PushMessageService {

    private final Environment env;

    private final WeChatServiceAccountConfig weChatServiceAccountConfig;

    @Autowired
    public PushMessageServiceImpl(Environment env, WeChatServiceAccountConfig weChatServiceAccountConfig) {
        this.env = env;
        this.weChatServiceAccountConfig = weChatServiceAccountConfig;
    }

    /**
     *
     * @param receiverAccount 接受者account
     * @param content
     * @param msgType 0 待审核提醒  1 审核结果通知 2 意见建议提醒  3 意见建议处理提醒  4 会议通知
     */
    @Override
    public void pushMsg(Account receiverAccount, int msgType, JSONObject content ) {
        //获取access_token
        WeChatAccessToken token = weChatServiceAccountConfig.getToken();

        //验证access_token是否有效
        if (!weChatServiceAccountConfig.verifyToken(token)) {
            token = weChatServiceAccountConfig.getToken();
        }

        //获取接收人的openID

        String openID = receiverAccount.getLoginWeChat().getWechatId();

        //获取模板id
        String templetedId = "";
        //获取小程序跳转路径
        String pagepath = env.getProperty("service_app.indexPagePath");

        //first
        JSONObject first = new JSONObject();
        first.put("color","#173177");

        //keyword1 第一个关键字
        JSONObject keyword1 = new JSONObject();
        keyword1.put("color","#173177");

        //keyword2 第二个关键字
        JSONObject keyword2 = new JSONObject();
        keyword2.put("color","#173177");

        //keyword3 第三个关键字
        JSONObject keyword3 = new JSONObject();
        keyword3.put("color","#173177");

        //keyword4 第四个关键字
        JSONObject keyword4 = new JSONObject();
        keyword4.put("color","#173177");

        //备注
        JSONObject remark = new JSONObject();
        remark.put("color","#173177");

        if (msgType == MsgTypeEnum.TO_AUDIT.ordinal()) {//待审核提醒
//            模版ID rtj7Pi26B75d1gMUnVwQFX-j4Iy8CJ5td9R18v2L0kc
//            标题 待审核提醒
//            行业 政府与公共事业 - 政府|公共事业|非盈利机构
//
//            详细内容：
//            {{first.DATA}}
//            审批事项：{{keyword1.DATA}}
//            业务类型：{{keyword2.DATA}}
//            流水编号：{{keyword3.DATA}}
//            递交时间：{{keyword4.DATA}}
//            {{remark.DATA}}

            //模板id
            templetedId = env.getProperty("service_app.templetedId1");
            pagepath = env.getProperty("service_app.npcHomePagePath");
            if(content.get("auditItem").equals("新闻")){
                pagepath = env.getProperty("service_app.newsAuditPath");
            }

            if(content.get("auditItem").equals("通知")){
                pagepath = env.getProperty("service_app.notificationAuditPath");
            }

            //first
            first.put("value",content.get("subtitle"));
            //审批事项
            keyword1.put("value",content.get("auditItem"));
            //业务类型
            keyword2.put("value",content.get("serviceType"));
            //流水号
            String number =new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            keyword3.put("value",number);
            //日期
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword4.put("value",time);
            //备注
            remark.put("value",content.get("remarkInfo"));
        }

        if (msgType == MsgTypeEnum.AUDIT_RESULT.ordinal()){//审核结果通知
//            模版ID HQunZ6pei-EbQY597Qr0qlb9qs54Fs0q9MEBzN6seJ0
//            标题 审核结果通知
//            行业 政府与公共事业 - 政府|公共事业|非盈利机构
//            详细内容
//            {{first.DATA}}
//            审核事项：{{keyword1.DATA}}
//            审核结果：{{keyword2.DATA}}
//            {{remark.DATA}}

            //模板id
            templetedId = env.getProperty("service_app.templetedId2");
            pagepath = env.getProperty("service_app.npcHomePagePath");

            //first
            first.put("value",content.get("subtitle"));
            //审核事项
            keyword1.put("value",content.get("auditItem"));
            //审核结果
            keyword2.put("value",content.get("result"));

            //备注
            remark.put("value",content.get("remarkInfo"));
        }

        if (msgType == MsgTypeEnum.NEW_OPINION_OR_SUGGESTION.ordinal()){
//            模版ID
//            S27PWGRD-Ze_OgLcyaqIgB4l57VBmX-p58s0MAAkWZ0
//            开发者调用模版消息接口时需提供模版ID
//                    标题
//            意见建议提醒
//                    行业
//            政府与公共事业 - 政府|公共事业|非盈利机构
//            详细内容
//            {{first.DATA}}
//            用户名：{{keyword1.DATA}}
//            联系方式：{{keyword2.DATA}}
//            提交时间：{{keyword3.DATA}}
//            内容：{{keyword4.DATA}}
//            {{remark.DATA}}

            //模板id
            templetedId = env.getProperty("service_app.templetedId3");
            pagepath = env.getProperty("service_app.npcHomePagePath");

            //first
            first.put("value",content.get("subtitle"));

            //用户名
            keyword1.put("value",content.get("accountName"));

            //联系方式
            keyword2.put("value",content.get("mobile"));

            //提交时间
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword3.put("value",time);

            //内容
            keyword4.put("value",content.get("content"));
            //备注
            remark.put("value",content.get("remarkInfo"));
        }
        if (msgType == MsgTypeEnum.FEEDBACK.ordinal()){//意见建议处理提醒

            //模板id
            templetedId = env.getProperty("service_app.templetedId4");
            pagepath = env.getProperty("service_app.npcHomePagePath");
            //first
            first.put("value",content.get("subtitle"));

            //标题
            keyword1.put("value",content.get("title"));

            //回复时间
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword2.put("value",time);

            //回复内容
            keyword3.put("value",content.get("content"));

            //备注
            remark.put("value",content.get("remarkInfo"));
        }
        if (msgType == MsgTypeEnum.CONFERENCE.ordinal()){

            //模板id
            templetedId = env.getProperty("service_app.templetedId5");
            pagepath = env.getProperty("service_app.npcHomePagePath");
            //first
            first.put("value",content.get("subtitle"));

            //会议时间
            keyword1.put("value",content.get("time"));

            //会议主题
            keyword2.put("value",content.get("theme"));

            //会议地点
            keyword3.put("value",content.get("place"));

            //备注
            remark.put("value",content.get("remarkInfo"));
        }

        JSONObject msgObj = new JSONObject();
        msgObj.put("touser",openID);
        msgObj.put("template_id",templetedId);

        JSONObject miniProgramObj = new JSONObject();
        //获取小程序appid
        String appId = env.getProperty("miniapp.appid");
        miniProgramObj.put("appid",appId);
        miniProgramObj.put("pagepath",pagepath);

        msgObj.put("miniprogram",miniProgramObj);

        //data
        JSONObject data = new JSONObject();
        data.put("first",first);
        data.put("keyword1",keyword1);
        data.put("keyword2",keyword2);
        data.put("keyword3",keyword3);
        data.put("keyword4",keyword4);
        data.put("remark",remark);

        msgObj.put("data",data);

        String msgBody = msgObj.toJSONString();

        //发送
        Certification.send(token.getAccessToken(), msgBody);
    }

}
