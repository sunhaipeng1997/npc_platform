package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.Code;
import com.cdkhd.npc.repository.base.CodeRepository;
import com.cdkhd.npc.service.CodeService;
import com.cdkhd.npc.util.BDSmsUtils;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class CodeServiceImpl implements CodeService {

    private Environment env;

    private CodeRepository codeRepository;

    @Autowired
    public CodeServiceImpl(Environment env, CodeRepository codeRepository) {
        this.env = env;
        this.codeRepository = codeRepository;
    }

    @Override
    public RespBody sendCode(String mobile) {
        RespBody body = new RespBody();
        //生成验证码，获取发送短信的配置参数
        int verifycode = new Random().nextInt(899999) + 100000; //每次调用生成一次六位数的随机数
        final String accessKeyId = env.getProperty("code.accessKeyId");
        final String accessKeySecret = env.getProperty("code.AccessKeySecret");
        final String endPoint = env.getProperty("code.endPoint");
        final String invokeId = env.getProperty("code.invokeId");
        final String templateCode = env.getProperty("code.templateCode");
        int timeout = Integer.parseInt(env.getProperty("code.timeout"));

        //发送短信验证码
        BDSmsUtils.sendSms(mobile, accessKeyId, accessKeySecret, verifycode, endPoint, invokeId, templateCode, timeout);

        //保存code
        Code code = codeRepository.findByMobile(mobile);
        if (code == null) {
            code = new Code();
        }
        code.setMobile(mobile);
        code.setCode(verifycode + "");
        code.setCreateTime(new Date());
        code.setValid(true);
        codeRepository.saveAndFlush(code);

        body.setStatus(HttpStatus.OK);
        return body;
    }
}
