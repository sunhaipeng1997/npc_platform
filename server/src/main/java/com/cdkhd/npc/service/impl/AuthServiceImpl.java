package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.UidDto;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.entity.vo.MenuVo;
import com.cdkhd.npc.enums.NpcMemberRoleEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.CodeRepository;
import com.cdkhd.npc.repository.base.LoginUPRepository;
import com.cdkhd.npc.repository.base.MenuRepository;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.util.BDSmsUtils;
import com.cdkhd.npc.util.JwtUtils;
import com.cdkhd.npc.vo.RespBody;
import com.cdkhd.npc.vo.TokenVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    private AccountRepository accountRepository;
    private LoginUPRepository loginUPRepository;
    private CodeRepository codeRepository;
    private MenuRepository menuRepository;
    private Environment env;

    @Autowired
    public AuthServiceImpl(AccountRepository accountRepository, LoginUPRepository loginUPRepository, CodeRepository codeRepository, MenuRepository menuRepository, Environment env) {
        this.accountRepository = accountRepository;
        this.loginUPRepository = loginUPRepository;
        this.codeRepository = codeRepository;
        this.menuRepository = menuRepository;
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
        Account account =  loginUPRepository.findByUsername(username).getAccount();
//        Account account = accountRepository.findByLoginUPUsername(username);
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
        int timeout = Integer.parseInt(env.getProperty("code.timeout"));
        String telephoneString = account.getLoginUP().getMobile();

        //发送短信验证码
        BDSmsUtils.sendSms(telephoneString, accessKeyId, accessKeySecret, verifycode, endPoint, invokeId, templateCode, timeout);

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

        Account account =  loginUPRepository.findByUsername(upDto.getUsername()).getAccount();
//        Account account = accountRepository.findByLoginUPUsername(upDto.getUsername());
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

    //根据相应的用户身份和选择的系统返回菜单
    @Override
    public RespBody menus(UserDetailsImpl userDetails, UidDto uidDto) {
        RespBody body = new RespBody();
        if (userDetails == null) {
            body.setMessage("用户未登录");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        if (account == null) {
            body.setMessage("账户不存在");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        if (account.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            body.setMessage("账号被禁用");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            JSONObject obj = new JSONObject();
            // 1 正常  2 被禁用
            obj.put("status", 2);
            body.setData(obj);
            return body;
        }
        if (StringUtils.isEmpty(uidDto.getUid())){
            body.setMessage("请选择系统");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        List<Menu> menus = Lists.newArrayList();//当前用户应该展示的菜单
        List<Menu> systemMenus = menuRepository.findBySystemsUidAndEnabled(uidDto.getUid(), StatusEnum.ENABLED.getValue());//当前系统下的所有菜单
        Set<AccountRole> accountRoles = account.getAccountRoles();
        if (CollectionUtils.isNotEmpty(accountRoles)) {
            for (AccountRole role : accountRoles) {//代表拥有的角色
                if (!role.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//确保角色状态有效
                Set<Permission> permissions = role.getPermissions();
                for (Permission permission : permissions) {
                    if (!permission.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//确保权限状态有效
                    Set<Menu> backMenus = permission.getMenus();//获取权限下的菜单
                    if (CollectionUtils.isNotEmpty(backMenus)) {
                        backMenus.retainAll(systemMenus);//当前权限下的菜单和当前系统下的菜单取交集
                        for (Menu menu : backMenus) {
                            if (!menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) continue;//菜单可用才展示
                            if (menu.getType().equals(StatusEnum.ENABLED.getValue())) continue;//如果是小程序菜单，就过滤掉
                            menus.add(menu);
                        }
                    }
                }
            }
        }
        menus.sort(Comparator.comparing(Menu::getId));//按id排序
        List<MenuVo> menuVos = this.dealChildren(menus);
        body.setData(menuVos);
        return body;
    }

    /**
     * 将子菜单放到对应的模块下
     * @param menus
     * @return
     */
    private List<MenuVo> dealChildren(List<Menu> menus) {
        List<MenuVo> menuVos = Lists.newArrayList();
        for (Menu menu : menus) {//所有的子级菜单
            if (menu.getParent()!= null){//把二级菜单装在一级菜单下
                Boolean isHave = false;
                for (MenuVo menuVo : menuVos) {//先处理一级菜单
                    if (menuVo.getUid().equals(menu.getParent().getUid())){
                        isHave = true;
                    }
                }
                if (!isHave){
                    menuVos.add(MenuVo.convert(menu.getParent()));
                }
                for (MenuVo menuVo : menuVos) {//再处理二级菜单
                    if (menuVo.getUid().equals(menu.getParent().getUid())){
                        List<MenuVo> children = menuVo.getChildren();
                        children.add(MenuVo.convert(menu));
                        menuVo.setChildren(children);
                    }
                }
            }
        }
        return menuVos;
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
        token.setUid(account.getUid());

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
