package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.AccountRole;
import com.cdkhd.npc.entity.Code;
import com.cdkhd.npc.entity.Token;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.CodeRepository;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.util.BDSmsUtils;
import com.cdkhd.npc.util.JwtUtils;
import com.cdkhd.npc.vo.RespBody;
import com.cdkhd.npc.vo.TokenVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
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

        Account account = accountRepository.findByLoginUPUsername(username);
        if (account == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名不存在，拒绝发送验证码");
            return body;
        }

        //生成验证码，获取发送短信的配置参数
        int verifycode = new Random().nextInt(899999) + 100000; //每次调用生成一次六位数的随机数
        final String accessKeyId = env.getProperty("code.accessKeyId");
        final String accessKeySecret = env.getProperty("code.AccessKeySecret");
        final String endPoint = env.getProperty("code.endPoint");
        final String invokeId = env.getProperty("code.invokeId");
        final String templateCode = env.getProperty("code.templateCode");
        String telephoneString = account.getLoginUP().getMobile();

        //发送短信验证码
        BDSmsUtils.sendSms(telephoneString, accessKeyId, accessKeySecret, verifycode, endPoint, invokeId, templateCode);

        //保存code
        Code code = codeRepository.findByMobile(telephoneString);
        if (code == null) {
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
        RespBody body = new RespBody<>();

        if (StringUtils.isEmpty(upDto.getUsername()) || StringUtils.isEmpty(upDto.getPassword())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名或密码不能为空");
            return body;
        }

        Account account = accountRepository.findByLoginUPUsername(upDto.getUsername());
        if (account == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名不存在");
            return body;
        }

        if (!account.getLoginUP().getPassword().equals(upDto.getPassword())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("密码错误");
            return body;
        }

        Code code = codeRepository.findByMobile(account.getLoginUP().getMobile());
        if (code == null || !code.getCode().equals(upDto.getCode())) {
            body.setMessage("验证码错误");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        String expireStr = env.getProperty("code.timeout");  //读取验证码过期分钟数
        int expireMinutes = 30;  //默认为三十分钟有效
        if (StringUtils.isNotBlank(expireStr)) {
            expireMinutes = Integer.parseInt(expireStr);
        }

        Date expireAt = DateUtils.addMinutes(code.getCreateTime(), expireMinutes);

        if (!code.getValid() || expireAt.before(new Date())) {
            body.setMessage("验证码已失效");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        if (account.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("账号已被禁用");
            return body;
        }

        //成功验证码验证过一次后设置为失效
        code.setValid(false);
        codeRepository.saveAndFlush(code);
        //生成token字符串
        TokenVo tokenVo = generateToken(account);
        body.setData(tokenVo);
        return body;
    }

    /**
     * 根据账号信息生成token
     *
     * @param account 生成token所需的账号信息
     * @return 生成的token字符串
     */
    private TokenVo generateToken(Account account) {
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
        token.setUsername(account.getLoginUP().getUsername());

        //设置角色信息
        Set<String> roleKeywords = new HashSet<>();
        for (AccountRole accountRole : account.getAccountRoles()) {
            roleKeywords.add(accountRole.getKeyword());
        }

        TokenVo tokenVo = new TokenVo();
        BeanUtils.copyProperties(token, tokenVo);
        token.setRoles(roleKeywords);
        tokenVo.setRoles(roleKeywords);
        tokenVo.setToken(JwtUtils.createJwt(token));
        //生成jwt token
        return tokenVo;
    }
}
