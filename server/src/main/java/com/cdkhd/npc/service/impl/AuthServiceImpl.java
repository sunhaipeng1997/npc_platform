package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.PasswordDto;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.entity.vo.MenuVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.util.BDSmsUtils;
import com.cdkhd.npc.util.JwtUtils;
import com.cdkhd.npc.utils.WXAppletUserInfo;
import com.cdkhd.npc.vo.RespBody;
import com.cdkhd.npc.vo.TokenVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    private AccountRepository accountRepository;
    private LoginUPRepository loginUPRepository;
    private CodeRepository codeRepository;

    private LoginWeChatRepository loginWeChatRepository;
    private AccountRoleRepository accountRoleRepository;

    private final Environment env;
    private final RestTemplate restTemplate;

    //服务号的appid和秘钥
    private final String CURRENT_APPID;
    private final String CURRENT_APPSECRET;
    private MenuRepository menuRepository;

    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    public AuthServiceImpl(AccountRepository accountRepository, LoginUPRepository loginUPRepository, CodeRepository codeRepository, LoginWeChatRepository loginWeChatRepository, AccountRoleRepository accountRoleRepository, Environment env, RestTemplate restTemplate, MenuRepository menuRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.loginUPRepository = loginUPRepository;
        this.codeRepository = codeRepository;
        this.loginWeChatRepository = loginWeChatRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.menuRepository = menuRepository;
        this.env = env;
        this.restTemplate = restTemplate;
        CURRENT_APPID = env.getProperty("service_app.appid");
        CURRENT_APPSECRET = env.getProperty("service_app.appsecret");
        this.passwordEncoder = passwordEncoder;
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

        if (loginUPRepository.findByUsername(username) == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名不存在, 拒绝发送验证码");
            return body;
        }

        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户名不存在，拒绝发送验证码");
            return body;
        }
        BackgroundAdmin bg = account.getBackgroundAdmin();
        if (bg.getLevel().equals(LevelEnum.TOWN.getValue()) && bg.getTown().getIsDel()){
            //改镇已经被删除 不能发验证码
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("改镇已经被删除，拒绝发送验证码");
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
//        String telephoneString = account.getLoginUP().getMobile();
        String telephoneString = account.getMobile();  //从account中取电话号码

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
        RespBody<TokenVo> body = new RespBody<>();

        //验证
        Account account;
        try {
            account = checkUPDto(upDto, true);
        } catch (Exception e) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(e.getMessage());
            return body;
        }

        BackgroundAdmin bg = account.getBackgroundAdmin();
        if (bg.getLevel().equals(LevelEnum.TOWN.getValue()) && bg.getTown().getIsDel()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该镇已经被删除，无法登录");
            return body;
        }

        //设置登录次数
        account.setLoginTimes(account.getLoginTimes() == null ? 1 : account.getLoginTimes() + 1);
        //设置上次登录时间
        if (account.getLoginTime() != null) {
            account.setLastLoginTime(account.getLoginTime());
        }
        //设置这次登录时间
        account.setLoginTime(new Date());
        account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
        accountRepository.saveAndFlush(account);

        //生成token字符串
        TokenVo tokenVo = generateToken(account);
        String unitName = account.getBackgroundAdmin().getLevel().equals(LevelEnum.AREA.getValue())?account.getBackgroundAdmin().getArea().getName():account.getBackgroundAdmin().getTown().getName();
        tokenVo.setUnitName(unitName);
        body.setData(tokenVo);
        return body;
    }

    //根据相应的用户身份和选择的系统返回菜单
    @Override
    public RespBody menus(UserDetailsImpl userDetails, BaseDto baseDto) {
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
        if (StringUtils.isEmpty(baseDto.getUid())) {
            body.setMessage("请选择系统");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        List<Menu> menus = Lists.newArrayList();//当前用户应该展示的菜单
        List<Menu> systemMenus = menuRepository.findBySystemsUidAndEnabled(baseDto.getUid(), StatusEnum.ENABLED.getValue());//当前系统下的所有菜单
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
                            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                                if (menu.getName().equals(MenuEnum.TOWN_MANAGE.getName()) ||
                                        menu.getName().equals(MenuEnum.TOWN_SUGGESTION_MANAGE.getName()) ||
                                        menu.getName().equals(MenuEnum.TOWN_PERFORMANCE_MANAGE.getName()) ||
                                        menu.getName().equals(MenuEnum.TOWN_PERFORMANCE_COUNT.getName())
                                ){
                                    continue;//镇后台管理员没有镇管理、各镇代表建议管理、各镇代表履职管理/也沒有系统设置
                                }
                                if (userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()) && (
                                    menu.getName().equals(MenuEnum.SUGGESTION_TYPE_MANAGE.getName()) ||
                                    menu.getName().equals(MenuEnum.PERFORMANCE_TYPE_MANAGE.getName()) ||
                                    menu.getName().equals(MenuEnum.SESSION_MANAGE.getName())
                                )){
                                    continue;//街道后台管理员没有履职类型管理、建议类型管理、届期管理
                                }
                            }
                            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) && (menu.getName().equals(MenuEnum.VILLAGE_MANAGE.getName()) || menu.getName().equals(MenuEnum.NPC_MEMBER_GROUP.getName())))
                                continue;//区后台管理员没有村管理
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

    @Override
    public RespBody updatePwd(UserDetailsImpl userDetails, PasswordDto passwordDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(passwordDto.getOldPwd())) {
            body.setMessage("旧密码不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (StringUtils.isEmpty(passwordDto.getNewPwd())) {
            body.setMessage("新密码不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (!passwordDto.getNewPwd().equals(passwordDto.getConfirmPwd())) {
            body.setMessage("两次输入密码不一致");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        LoginUP loginUP = accountRepository.findByUid(userDetails.getUid()).getLoginUP();
        if (!passwordEncoder.matches(passwordDto.getOldPwd(), loginUP.getPassword())) {
            body.setMessage("旧密码错误");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (passwordDto.getNewPwd().length() < 6) {
            body.setMessage("新密码长度至少6位");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        loginUP.setPassword(passwordEncoder.encode(passwordDto.getNewPwd()));
        loginUPRepository.saveAndFlush(loginUP);
        return body;
    }

    //大数据平台的登录业务，无短信验证码
    @Override
    public RespBody bigDataLogin(UsernamePasswordDto upDto) {
        RespBody<TokenVo> body = new RespBody<>();

        //验证
        Account account;
        try {
            account = checkUPDto(upDto, false);
        } catch (Exception e) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(e.getMessage());
            return body;
        }

        //设置登录次数
        account.setLoginTimes(account.getLoginTimes() == null ? 1 : account.getLoginTimes() + 1);
        //设置上次登录时间
        if (account.getLoginTime() != null) {
            account.setLastLoginTime(account.getLoginTime());
        }
        //设置这次登录时间
        account.setLoginTime(new Date());
        account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
        accountRepository.saveAndFlush(account);

        //生成token
        TokenVo tokenVo = generateToken(account);

        body.setData(tokenVo);
        return body;
    }

    /**
     * 将子菜单放到对应的模块下
     *
     * @param menus
     * @return
     */
    private List<MenuVo> dealChildren(List<Menu> menus) {
        List<MenuVo> menuVos = Lists.newArrayList();
        for (Menu menu : menus) {//所有的子级菜单
            if (menu.getParent() != null) {//把二级菜单装在一级菜单下
                Boolean isHave = false;
                for (MenuVo menuVo : menuVos) {//先处理一级菜单
                    if (menuVo.getUid().equals(menu.getParent().getUid())) {
                        isHave = true;
                    }
                }
                if (!isHave) {
                    menuVos.add(MenuVo.convert(menu.getParent()));
                }
                for (MenuVo menuVo : menuVos) {//再处理二级菜单
                    if (menuVo.getUid().equals(menu.getParent().getUid())) {
                        List<MenuVo> children = menuVo.getChildren();
                        children.add(MenuVo.convert(menu));
                        menuVo.setChildren(children);
                    }
                }
            } else {
                menuVos.add(MenuVo.convert(menu));
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

        //李亚林
        //登录方式不同，则用户名的设置也不同，尽可能保证用户名唯一
        if (account.getLoginWay().equals(LoginWayEnum.LOGIN_UP.getValue())) {
            token.setUsername(account.getLoginUP().getUsername());
        } else if (account.getLoginWay().equals(LoginWayEnum.LOGIN_WECHAT.getValue())) {
            //如果是微信登录方式，则用户名暂时设定为UnionId
            token.setUsername(account.getLoginWeChat().getUnionId());
        }
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

    /**
     * 微信小程序登录凭证校验
     * <p>
     * 注：小程序认证比较特殊，
     * 使用appid、appsecret和临时code作为参数向微信接口服务器请求session_key和openid
     */
    private ResponseEntity<String> code2session(String code) {
        String authUrl = env.getProperty("miniapp.authurl");
        String appId = env.getProperty("miniapp.appid");
        String appSecret = env.getProperty("miniapp.appsecret");

        //GET请求地址：https://api.weixin.qq.com/sns/jscode2session
        //参数：appid、secret、js_code、grant_type
        //通过 wx.login 接口获得临时登录凭证 code 后传到开发者服务器调用此接口完成登录流程。
        String reqUrl = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", authUrl, appId, appSecret, code);

        //返回值：openid、session_key、unionid（用户在开放平台的唯一标识符）
        ResponseEntity<String> resp = new RestTemplate().getForEntity(reqUrl, String.class);
        return resp;
    }

    /**
     * 验证UsernamePasswordDto
     * @param upDto 待验证的DTO
     * @param checkCode 是否验证短信验证码
     * @return 相应的Account
     * @throws Exception 包含验证失败信息的异常
     */
    private Account checkUPDto(UsernamePasswordDto upDto, boolean checkCode) throws Exception {
        if (StringUtils.isEmpty(upDto.getUsername()) || StringUtils.isEmpty(upDto.getPassword()))
            throw new Exception("用户名/密码不能为空");

        LoginUP loginUP = loginUPRepository.findByUsername(upDto.getUsername());
        if (loginUP == null)
            throw new Exception("用户名不存在");

        Account account = loginUP.getAccount();
        if (account == null) {
            LOGGER.error("login_UP无对应的Account，请检查数据库数据");
            throw new Exception("用户不存在");
        }

        if (!passwordEncoder.matches(upDto.getPassword(), loginUP.getPassword()))
            throw new Exception("密码错误");

        if (checkCode) {
            Code code = codeRepository.findByMobile(account.getMobile());
            checkCode(upDto.getCode(), code);
            //短信验证码成功验证过一次后设置为失效
            code.setValid(false);
            codeRepository.saveAndFlush(code);
        }

        if (account.getStatus().equals(StatusEnum.DISABLED.getValue()))
            throw new Exception("账号已被禁用");

        return account;
    }

    /**
     * 验证短信验证码
     * @param toCheck 待验证的短信验证码
     * @param code 正确的验证码对象
     * @throws Exception 包含验证失败信息的异常
     */
    private void checkCode(String toCheck, Code code) throws Exception {
        if (code == null || !code.getCode().equals(toCheck))
            throw new Exception("验证码错误");

        int expireMinutes = 30;  //默认为三十分钟有效
        String expireStr = env.getProperty("code.timeout");  //读取验证码过期分钟数
        if (StringUtils.isNotBlank(expireStr)) {
            expireMinutes = Integer.parseInt(expireStr);
        }

        Date expireAt = DateUtils.addMinutes(code.getCreateTime(), expireMinutes);
        if (!code.getValid() || expireAt.before(new Date()))
            throw new Exception("验证码已失效");
    }

    /**
     *
     */
    @Override
    public RespBody auth(String nickName, String code, String encryptedData, String iv) {
        RespBody body = new RespBody();

        //登录凭证校验结果
        ResponseEntity<String> resp = this.code2session(code);

        if (HttpStatus.OK == resp.getStatusCode()) {
            JSONObject json = JSON.parseObject(resp.getBody());
            String errcode = json.getString("errcode");

            //用户在开放平台的唯一标识符unionid，在满足 UnionID 下发条件的情况下会返回
            String unionid = json.getString("unionid");

            //errcode合法值：-1(系统繁忙，此时请开发者稍候再试)、0(请求成功)、40029(code无效)、45011(频率限制，每个用户每分钟100次)
            if (StringUtils.isNotBlank(errcode)) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("微信服务器系统繁忙或者code无效，或者操作过于频繁");
                return body;
            }

            //如果unionid为空
            if (StringUtils.isEmpty(unionid)) {
                String sessionKey = json.getString("session_key");
                String result = WXAppletUserInfo.getUserInfo(encryptedData, sessionKey, iv);//String encryptedData,String sessionKey,String iv
                JSONObject resultJson = JSON.parseObject(result);
                unionid = resultJson.getString("unionId");
            }

            String openid = json.getString("openid");

            LoginWeChat loginWeChat = loginWeChatRepository.findByUnionId(unionid);
            if (loginWeChat == null) {

                JSONObject obj = new JSONObject();
                // 5 未注册
                obj.put("status", 5);

                RespBody<JSONObject> respBody = new RespBody<>();
                respBody.setData(obj);
                respBody.setStatus(HttpStatus.BAD_REQUEST);
                respBody.setMessage("还未注册，请您先注册");
                return respBody;
            }

            Account account = loginWeChat.getAccount();
            if (account == null) {
                loginWeChatRepository.delete(loginWeChat);

                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("还未注册，请您先注册");

                JSONObject obj = new JSONObject();
                // 5 未注册
                obj.put("status", 5);

                RespBody<JSONObject> respBody = new RespBody<>();
                respBody.setData(obj);
                return respBody;
            }

            if (account.getStatus() == StatusEnum.DISABLED.getValue()) {
                body.setMessage("账号被禁用");
                body.setStatus(HttpStatus.UNAUTHORIZED);
                JSONObject obj = new JSONObject();
                // 1 正常  2 被禁用
                obj.put("status", StatusEnum.DISABLED.getName());

                RespBody<JSONObject> respBody = new RespBody<>();
                respBody.setData(obj);
                return respBody;
            }

            account.setLastLoginTime(account.getLoginTime());
            account.setLoginTimes(account.getLoginTimes() + 1);
            account.setLoginTime(new Date());

            //生成token
            TokenVo tokenVo = generateToken(account);

            body.setData(tokenVo);
            return body;
        }

        body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        body.setMessage("获取Token失败，请稍后再试");
        return body;
    }

    @Override
    public String accessToken(String code, String state) {
        // 获取access_token
        String getUserAccessTokenUrl = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                CURRENT_APPID,
                CURRENT_APPSECRET,
                code
        );

        ResponseEntity<String> respEntity = restTemplate.getForEntity(getUserAccessTokenUrl, String.class);
        if (respEntity.getStatusCode() != HttpStatus.OK) {
            return "authFail";
        }

        String respBody = respEntity.getBody();
        if (respBody == null || StringUtils.isBlank(respBody)) {
            return "authFail";
        }

        JSONObject obj = JSON.parseObject(respBody);

        // 获取用户信息
        String getUserInfoUrl = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                obj.getString("access_token"),
                CURRENT_APPID
        );
        respEntity = restTemplate.getForEntity(getUserInfoUrl, String.class);
        if (respEntity.getStatusCode() != HttpStatus.OK) {
            return "authFail";
        }

        respBody = respEntity.getBody();
        if (respBody == null || StringUtils.isBlank(respBody)) {
            return "authFail";
        }

        obj = JSON.parseObject(respBody);

        String unionid = obj.getString("unionid");
        String openid = obj.getString("openid");
        String nickname = obj.getString("nickname");

        LoginWeChat loginWeChat = loginWeChatRepository.findByUnionId(unionid);
        if (loginWeChat == null) {
            loginWeChat = new LoginWeChat();
            loginWeChat.setOpenId(openid);
            loginWeChat.setUnionId(unionid);

            loginWeChatRepository.saveAndFlush(loginWeChat);
        }

        Account account = loginWeChat.getAccount();
        if (account == null) {
            account = new Account();
            account.setLoginWay(LoginWayEnum.LOGIN_WECHAT.getValue());
            account.setLoginWeChat(loginWeChat);
            account.setStatus(StatusEnum.ENABLED.getValue());

            //初始时只有选民权限
            account.getAccountRoles().add(accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword()));
            accountRepository.saveAndFlush(account);

            loginWeChat.setAccount(account);
            loginWeChatRepository.saveAndFlush(loginWeChat);

        }

        return "authSuccess";
    }

}
