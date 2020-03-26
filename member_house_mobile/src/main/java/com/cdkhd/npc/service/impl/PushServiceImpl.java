package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.service.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @创建人
 * @创建时间 2018/10/26
 * @描述
 */
@Service
public class PushServiceImpl implements PushService {

    private final Environment env;


    @Autowired
    public PushServiceImpl(Environment env) {
        this.env = env;
    }

    /**
     *
     * @param account
     * @param msg
     * @param type 1 待审核提醒  2 审核通过通知 3 投诉建议通知  4 会议通知  5 意见建议回复提醒
     */
    @Override
    public void pushMsg(Account account, String msg, Integer type, String keyWord) {

        //获取模板id
        String templetedId = "";
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

        if (type == 1) {
            //模板id
            templetedId = env.getProperty("service_app.templetedId1");
            //first
            first.put("value","您有一条待审核的信息");
            //审批事项
            keyword1.put("value",keyWord);
            //业务类型
            keyword2.put("value","审核业务");
            //流水号
            String number =new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            keyword3.put("value",number);
            //日期
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword4.put("value",time);
            //备注
            remark.put("value",msg);
        }else if (type == 2){
            //模板id
            templetedId = env.getProperty("service_app.templetedId2");
            //first
            first.put("value","您提交的信息已经审核通过");
            //活动名称
            keyword1.put("value",keyWord);
            //审核结果
            keyword2.put("value","通过");
            //审核日期
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword3.put("value",time);
            //备注
            remark.put("value",msg);
        }else if (type == 3){
            //模板id
            templetedId = env.getProperty("service_app.templetedId3");
            //first
            first.put("value","您收到一条新的投诉建议");
            //时间
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword1.put("value",time);
            //内容
            keyword2.put("value",msg);
            //备注
            remark.put("value","点击前往小程序查看详情！");
        }else if (type == 4){
            //模板id
            templetedId = env.getProperty("service_app.templetedId4");
            //first
            first.put("value","您收到了一条新的通知公告");
            //活动名称
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword1.put("value",time);
            //说明
            keyword2.put("value",keyWord);
            //内容
            keyword3.put("value",msg);
            //备注
            remark.put("value","点击前往小程序查看详情！");
        }else if (type == 5){
            //模板id
            templetedId = env.getProperty("service_app.templetedId5");
            //first
            keyword1.put("value","你提交的意见建议有新的回复");
            //时间
            String time =new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date());
            keyword2.put("value",time);
            //回复内容
            keyword3.put("value",msg);
            //备注
            remark.put("value","点击前往小程序查看详情！");
        }

        //获取小程序appid
        String appId = env.getProperty("miniapp.appid");

        //获取小程序跳转路径
        String pagepath = env.getProperty("service_app.path");

        JSONObject obj = new JSONObject();
//        obj.put("touser",openID);
        obj.put("template_id",templetedId);
        JSONObject obj1 = new JSONObject();
        obj1.put("appid",appId);
        obj1.put("pagepath",pagepath);
        obj.put("miniprogram",obj1);


        //data
        JSONObject data = new JSONObject();
        data.put("first",first);
        data.put("keyword1",keyword1);
        data.put("keyword2",keyword2);
        data.put("keyword3",keyword3);
        data.put("keyword4",keyword4);
        data.put("remark",remark);

        obj.put("data",data);

        String body1 = obj.toJSONString();

    }

}
