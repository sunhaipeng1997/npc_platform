package com.cdkhd.npc.api;

import com.cdkhd.npc.entity.dto.RequestDto;

import com.cdkhd.npc.util.MessageUtil;
import com.cdkhd.npc.utils.CheckoutUtil;

import org.dom4j.DocumentException;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @创建人 lizi
 * @创建时间 2018/10/16
 * @描述
 */
@Controller
@RequestMapping("/api/mini_app/push")
public class PushApi {

    private final Environment env;

    public PushApi(Environment env) {
        this.env = env;
    }

    @GetMapping
    public ResponseEntity<String> weixin(RequestDto dto) {

        //验证的token
        String token = env.getProperty("service_app.token");

        // 微信加密签名
        String signature = dto.getSignature();
        //时间戳
        String timestamp = dto.getTimestamp();
        //随机数
        String nonce = dto.getNonce();
        //随机字符串
        String echostr = dto.getEchostr();
        //通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        if (signature != null && CheckoutUtil.checkSignature(signature, timestamp, nonce,token)) {
            return ResponseEntity.ok(echostr);
        }

        return ResponseEntity.ok("invalid server");
    }

    @PostMapping
    public PrintWriter receive(HttpServletRequest req, HttpServletResponse resp) throws IOException, DocumentException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String message;
            Map<String, String> map = MessageUtil.xmlToMap(req);
            String toUserName = map.get("ToUserName");
            String fromUserName = map.get("FromUserName");
            String msgType = map.get("MsgType");
            String content = map.get("Content");
        //判断请求是否事件类型 event
            if (MessageUtil.MESSAGE_EVENT.equals(msgType)) {
                String eventType = map.get("Event");
                //若是关注事件  subscribe
                if (MessageUtil.EVENT_SUB.equals(eventType)) {
                    String mycontent = MessageUtil.menuText();
                    message = MessageUtil.initText(toUserName, fromUserName, mycontent);
                    out.write(message);
                    out.flush();
                } else {
                    return null;
                }
            }
            return null;
    }

}
