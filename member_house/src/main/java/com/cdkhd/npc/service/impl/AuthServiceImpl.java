package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.repository.AccountRepository;
import com.cdkhd.npc.repository.CodeRepository;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.util.BDSmsUtils;
import com.cdkhd.npc.util.Constant;
import com.cdkhd.npc.util.JwtUtils;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private AccountRepository accountRepository;
    private CodeRepository codeRepository;
    private Environment env;

    @Autowired
    public AuthServiceImpl(AccountRepository accountRepository, CodeRepository codeRepository, Environment env) {
        this.accountRepository = accountRepository;
        this.codeRepository = codeRepository;
        this.env = env;
    }

    //生成并发送短信验证码
    @Override
    public RespBody getCode(String username) {
        RespBody body = new RespBody();

        if (StringUtils.isEmpty(username)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名不能为空");
            return body;
        }

        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名不存在，拒绝发送验证码");
            return body;
        }

        //生成验证码，获取发送短信的配置参数
        int verifycode = new Random().nextInt(8999) + 1000; //每次调用生成一次四位数的随机数
        final String accessKeyId = env.getProperty("code.accessKeyId");
        final String accessKeySecret = env.getProperty("code.AccessKeySecret");
        final String endPoint = env.getProperty("code.endPoint");
        final String invokeId = env.getProperty("code.invokeId");
        final String templateCode = env.getProperty("code.templateCode");
        String telephoneString = account.getMobile();

        //发送短信验证码
        BDSmsUtils.sendSms(telephoneString,accessKeyId,accessKeySecret,verifycode,endPoint,invokeId,templateCode);

        //保存code
        Code code = codeRepository.findByMobile(telephoneString);
        if (code == null){
            code = new Code();
        }
        code.setMobile(telephoneString);
        code.setCode(verifycode + "");
        code.setCreateTime(new Date());
        code.setValid(true);
        codeRepository.saveAndFlush(code);

        body.setStatus(HttpStatus.OK);
        return body;
    }

    //登录获取token
    @Override
    public RespBody login(UsernamePasswordDto upDto) {
        RespBody<String> body = new RespBody<>();

        if (StringUtils.isEmpty(upDto.getUsername()) || StringUtils.isEmpty(upDto.getPassword())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名或密码不能为空");
            return body;
        }

        /*if (StringUtils.isEmpty(upDto.getCode())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("验证码不能为空");
            return body;
        }*/

        Account account = accountRepository.findByUsername(upDto.getUsername());
        if (account == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名不存在");
            return body;
        }

        /*Code code = codeRepository.findByMobile(account.getMobile());
        if (code == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("请首先获取验证码");
            return body;
        }

        if (!code.isValid()) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("验证码已失效，请重新获取");
            return body;
        }

        if (!code.getCode().equals(upDto.getCode())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("验证码错误");
            return body;
        }*/

        if (!account.getPassword().equals(upDto.getPassword())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("密码错误");
            return body;
        }

        if (account.getStatus().equals(Constant.DISABLED)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("账号已被禁用");
            return body;
        }

        /*//验证码成功验证过一次后设置为失效
        code.setValid(false);
        codeRepository.saveAndFlush(code);*/

        //生成token字符串
        String jws = generateToken(account);

        body.setData(jws);
        return body;
    }

    /**
     * 根据账号信息生成token
     *
     * @param account 生成token所需的账号信息
     * @return 生成的token字符串
     */
    private String generateToken(Account account) {
        Token token = new Token();

        Date signAt = new Date();
        token.setSignAt(signAt);

        //从配置文件中获取token过期时间
        String expireStr = env.getProperty("token.expire");
        //默认为30天以后过期
        int expireDate = 30;
        if (StringUtils.isNotBlank(expireStr)) {
            expireDate = Integer.parseInt(expireStr);
        }
        Date expireAt = DateUtils.addDays(signAt, expireDate);
        token.setExpireAt(expireAt);

        token.setUsername(account.getUsername());

        //设置角色信息
        Set<String> roleKeywords = new HashSet<>();
        for (Role role : account.getRoles()) {
            roleKeywords.add(role.getName());
        }
        token.setRoles(roleKeywords);

        //生成jwt token
        return JwtUtils.createJwt(token);
    }
}
