package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.PhoneNumberDto;
import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.entity.vo.RelationVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.member_house.VillageRepository;
import com.cdkhd.npc.service.RegisterService;
import com.cdkhd.npc.util.BDSmsUtils;
import com.cdkhd.npc.util.JwtUtils;
import com.cdkhd.npc.utils.WXAppletUserInfo;
import com.cdkhd.npc.vo.RespBody;
import com.cdkhd.npc.vo.TokenVo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegisterServiceImpl implements RegisterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServiceImpl.class);
    private SystemRepository systemRepository;
    private AccountRepository accountRepository;
    private AccountRoleRepository accountRoleRepository;
    private NpcMemberRepository npcMemberRepository;
    private VoterRepository voterRepository;

    private AreaRepository areaRepository;
    private TownRepository townRepository;
    private VillageRepository villageRepository;

    private CodeRepository codeRepository;
    private LoginWeChatRepository loginWeChatRepository;
    private LoginUPRepository loginUPRepository;
    private MobileUserPreferencesRepository mobileUserPreferencesRepository;

    private final Environment env;

    @Autowired
    public RegisterServiceImpl(SystemRepository systemRepository, AccountRepository accountRepository, AccountRoleRepository accountRoleRepository, NpcMemberRepository npcMemberRepository, VoterRepository voterRepository, AreaRepository areaRepository, TownRepository townRepository, VillageRepository villageRepository, CodeRepository codeRepository, LoginWeChatRepository loginWeChatRepository, LoginUPRepository loginUPRepository, MobileUserPreferencesRepository mobileUserPreferencesRepository, Environment env) {
        this.systemRepository = systemRepository;
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.voterRepository = voterRepository;
        this.areaRepository = areaRepository;
        this.townRepository = townRepository;
        this.villageRepository = villageRepository;
        this.codeRepository = codeRepository;
        this.loginWeChatRepository = loginWeChatRepository;
        this.loginUPRepository = loginUPRepository;
        this.mobileUserPreferencesRepository = mobileUserPreferencesRepository;
        this.env = env;
    }




    @Override
    public RespBody getRelations() {
        RespBody body = new RespBody();
        List<Area> areas = areaRepository.findByStatus(StatusEnum.ENABLED.getValue());
        List<RelationVo> areaVos = Lists.newArrayList();
        for (Area area : areas) {
            RelationVo areaVo = RelationVo.convert(area.getUid(),area.getName(),LevelEnum.AREA.getValue(),area.getCreateTime());
            List<RelationVo> townVos = Lists.newArrayList();
            for (Town town : area.getTowns()) {
                if (town.getStatus().equals(StatusEnum.ENABLED.getValue()) && !town.getIsDel()) {
                    RelationVo townVo = RelationVo.convert(town.getUid(), town.getName(),LevelEnum.TOWN.getValue(),town.getCreateTime());
                    townVo.setChildren(town.getVillages().stream().map(village -> RelationVo.convert(village.getUid(), village.getName(),LevelEnum.TOWN.getValue(), village.getCreateTime())).sorted(Comparator.comparing(RelationVo::getCreateTime)).collect(Collectors.toList()));
                    townVos.add(townVo);
                }
            }
            townVos.sort(Comparator.comparing(RelationVo::getCreateTime));
            areaVo.setChildren(townVos);
            areaVos.add(areaVo);
        }
        body.setData(areaVos);
        return body;
    }



    @Override
    public RespBody getVerificationCode(PhoneNumberDto dto){
        RespBody body = new RespBody();

        //生成验证码，获取发送短信的配置参数
        int verifyCode = new Random().nextInt(899999) + 100000; //每次调用生成一次六位数的随机数
        final String accessKeyId = env.getProperty("code.accessKeyId");
        final String accessKeySecret = env.getProperty("code.AccessKeySecret");
        final String endPoint = env.getProperty("code.endPoint");
        final String invokeId = env.getProperty("code.invokeId");
        final String templateCode = env.getProperty("code.templateCode");
        int timeout = Integer.parseInt(env.getProperty("code.timeout"));

        String telephoneString = dto.getPhoneNumber();

        //发送短信验证码
        BDSmsUtils.sendSms(telephoneString, accessKeyId, accessKeySecret, verifyCode, endPoint, invokeId, templateCode, timeout);

        //保存code
        Code code = codeRepository.findByMobile(telephoneString);
        if (code == null) {
            code = new Code();
        }
        code.setMobile(telephoneString);
        code.setCode(verifyCode + "");
        code.setCreateTime(new Date());
        code.setValid(true);
        codeRepository.saveAndFlush(code);

        body.setStatus(HttpStatus.OK);
        return body;
    }

    /*
     *  第一步，校对验证码
     */
    private RespBody verifyCode(UserInfoDto dto) {
        RespBody body = new RespBody<>();
        Code code = codeRepository.findByMobile(dto.getMobile());
        if (code == null || !code.getCode().equals(dto.getVerificationCode())) {
            body.setMessage("验证码错误");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        //读取验证码过期分钟数
        String expireStr = env.getProperty("code.timeout");
        //默认为三十分钟有效
        int expireMinutes = 30;
        if (StringUtils.isNotBlank(expireStr)) {
            expireMinutes = Integer.parseInt(expireStr);
        }

        Date expireAt = DateUtils.addMinutes(code.getCreateTime(), expireMinutes);

        if (!code.getValid() || expireAt.before(new Date())) {
            body.setMessage("验证码已失效");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        //成功验证一次后，置为失效
        code.setValid(false);
        codeRepository.saveAndFlush(code);
        body.setMessage("验证通过");
        body.setStatus(HttpStatus.OK);

        return body;
    }

    /*
     *  第二步，验证用户信息
     */
    private RespBody verifyUserInfo(UserInfoDto dto) {
        RespBody body = new RespBody<>();

        //第二步，验证手机号码是否已经存在
        List<Account> accounts = accountRepository.findByMobile(dto.getMobile());
        Account currentAccount = null;
        for (Account account : accounts) {
            //判断账号的身份，将后台管理员给过滤掉
//            List<String> keywords = account.getAccountRoles().stream().filter(role -> !role.getKeyword().equals(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword())).map(AccountRole::getKeyword).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(keywords))
            if (account.getVoter() != null){
                //账号没有包含后台管理员，则表示已经注册了
                currentAccount = account;
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("该手机号已经注册,请登录");
                return body;
            }
        }
        //账户为空，则建立账户并关联相关数据库表
        if(currentAccount == null){

            //再去查询该手机号对应的代表，之所以有多个NpcMember,是表示不同的任职情况，并非是多个人大代表
            List<NpcMember> npcMembers = npcMemberRepository.findByMobileAndIsDelFalse(dto.getMobile());

            if (!npcMembers.isEmpty()){
                if(!npcMembers.get(0).getName().equals(dto.getName())){
                    body.setStatus(HttpStatus.BAD_REQUEST);
                    body.setMessage("代表姓名与系统中不一致");
                    return body;
                }
                currentAccount = createAccount(dto,AccountRoleEnum.NPC_MEMBER.getName());
            }
            else {//如果后台并没有添加该代表的信息，那么就默认注册为选民
                currentAccount = createAccount(dto,AccountRoleEnum.VOTER.getName());
            }
        }
        body.setMessage("验证通过");
        body.setStatus(HttpStatus.OK);

        return body.setData(currentAccount.getUid());
    }

    //根据账户角色创建账户
    private Account createAccount(UserInfoDto dto,String keyword){
        Account account = new Account();
        account.setStatus(StatusEnum.ENABLED.getValue());
        account.setLoginWay(LoginWayEnum.LOGIN_WECHAT.getValue());
        account.setMobile(dto.getMobile());
        account.setLoginTimes(account.getLoginTimes()+1);
        account.setLoginTime(new Date());
        account.setLastLoginTime(account.getLoginTime());
        account.setSystems(systemRepository.findByKeyword("MEMBER_HOUSE"));
        account.getAccountRoles().add(accountRoleRepository.findByKeyword("VOTER"));
        account.setMobileUserPreferences(new MobileUserPreferences());
        account.setGovernmentUser(null);
        account.setUnitUser(null);
        account.setLoginWeChat(new LoginWeChat());
        account.setLoginUP(null);//暂时不用管后台管理员，即便是同一人也不关联
        account.setVoter(new Voter());
        accountRepository.save(account);

        //任何人都有选民身份
        Voter voter = account.getVoter();
        voter.setMobile(dto.getMobile());
        voter.setRealname(dto.getName());
        voter.setGender(dto.getGender());
        voter.setAge(dto.getAge());
        voter.setBirthday(dto.getBirthday());//增加出生年月

        if(keyword.equals(AccountRoleEnum.VOTER.getName())){
            if(StringUtils.isNotEmpty(dto.getAreaUid())){
                voter.setArea(areaRepository.findByUid(dto.getAreaUid()));
            }

            if(StringUtils.isNotEmpty(dto.getTownUid())){
                voter.setTown(townRepository.findByUid(dto.getTownUid()));
            }

            if(StringUtils.isNotEmpty(dto.getVillageUid())){
                voter.setVillage(villageRepository.findByUid(dto.getVillageUid()));
            }
        }
        if(keyword.equals(AccountRoleEnum.NPC_MEMBER.getName())){
            List<NpcMember> npcMembers = npcMemberRepository.findByMobileAndIsDelFalse(dto.getMobile());
            Village theVillage = new Village();
            if(StringUtils.isNotEmpty(dto.getVillageUid())){
                 theVillage = villageRepository.findByUid(dto.getVillageUid());
            }

            if (!npcMembers.isEmpty()){
                voter.setArea(npcMembers.get(0).getArea());
                voter.setTown(npcMembers.get(0).getTown());
                if(npcMembers.get(0).getLevel().equals(LevelEnum.AREA.getValue())){
                    if(npcMembers.get(0).getTown().getVillages().contains(theVillage)){
                        voter.setVillage(theVillage);
                    }else{
                        voter.setVillage(npcMembers.get(0).getTown().getVillages().iterator().next());
                    }
                }else{
                    if(npcMembers.get(0).getNpcMemberGroup().getVillages().contains(theVillage)){
                        voter.setVillage(theVillage);
                    }else{
                        voter.setVillage(npcMembers.get(0).getNpcMemberGroup().getVillages().iterator().next());
                    }
                }
            }
        }


        voter.setAccount(accountRepository.findByUid(account.getUid()));
        voterRepository.save(voter);

        MobileUserPreferences mobileUserPreferences = account.getMobileUserPreferences();
        mobileUserPreferences.setShortcutAction(ShortcutActionEnum.GIVE_ADVICE.getName());

        //如果是代表身份
        if(keyword.equals(AccountRoleEnum.NPC_MEMBER.getName())){
            List<NpcMember> npcMembers = npcMemberRepository.findByMobileAndIsDelFalse(dto.getMobile());
            for (NpcMember npcMember:npcMembers){
                npcMember.setAccount(account);
                npcMemberRepository.save(npcMember);
                account.getNpcMembers().add(npcMember);
            }
            account.getAccountRoles().add(accountRoleRepository.findByKeyword("NPC_MEMBER"));
            mobileUserPreferences.setShortcutAction(ShortcutActionEnum.MAKE_SUGGESTION.getName());
        }
        mobileUserPreferences.setAccount(accountRepository.findByUid(account.getUid()));
        mobileUserPreferencesRepository.save(mobileUserPreferences);

        accountRepository.save(account);

        return account;
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
        if(account.getLoginWay().equals(LoginWayEnum.LOGIN_UP.getValue())){
            token.setUsername(account.getLoginUP().getUsername());
        }else if(account.getLoginWay().equals(LoginWayEnum.LOGIN_WECHAT.getValue())){
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
     *
     * 注：小程序认证比较特殊，
     * 使用appid、appsecret和临时code作为参数向微信接口服务器请求session_key和openid
     */
    private ResponseEntity<String> code2session(String code){
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
     *
     */
    @Override
    public RespBody register(UserInfoDto dto) {
        RespBody body = new RespBody();

        //先校验短信验证码
        body = verifyCode(dto);
        if(!body.getStatus().equals(HttpStatus.OK)){
            return body;
        }

        //接下来与微信接口服务器交互

        //登录凭证校验结果
        ResponseEntity<String> resp = this.code2session(dto.getCode());
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
            if(StringUtils.isEmpty(unionid)) {
                String sessionKey = json.getString("session_key");

                String result = WXAppletUserInfo.getUserInfo(dto.getEncryptedData(),sessionKey,dto.getIv());
                JSONObject resultJson = JSON.parseObject(result);
                unionid = resultJson.getString("unionId");
            }

            String openid = json.getString("openid");

            LoginWeChat theWeChat = loginWeChatRepository.findByUnionId(unionid);

            if(theWeChat != null){
                body.setMessage("账户已经注册，请登录");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }

            //再校验其他表单信息,并创建账户
            body = verifyUserInfo(dto);
            if(!body.getStatus().equals(HttpStatus.OK)){
                return body;
            }
            //成功创建好的账户
            String currentAccountUid = (String) body.getData();
            Account account = accountRepository.findByUid(currentAccountUid);
            if(account == null){
                body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                body.setMessage("账户创建失败，服务器内部错误");
                return body;
            }

            //再创建loginWeChat，并与账户关联起来
            LoginWeChat loginWeChat = account.getLoginWeChat();
            loginWeChat.setNickname(dto.getNickName());
            loginWeChat.setOpenId(openid);
            loginWeChat.setUnionId(unionid);
            loginWeChat.setAccount(accountRepository.findByUid(currentAccountUid));
            loginWeChatRepository.saveAndFlush(loginWeChat);

            account.setLoginWay((byte) 2);
            account.setLoginWeChat(loginWeChat);
            account.setUsername(dto.getNickName());
            accountRepository.saveAndFlush(account);

            //由账户生成token
            TokenVo tokenVo = generateToken(account);
            if(tokenVo == null){
                //之前建立的相关数据要全部删除
                loginWeChatRepository.delete(loginWeChat);
                voterRepository.delete(account.getVoter());
                mobileUserPreferencesRepository.delete(account.getMobileUserPreferences());
                accountRepository.delete(account);
                body.setMessage("生成Token失败");
                body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                return body;
            }

            body.setData(tokenVo);
            body.setMessage("注册并登录成功");
            body.setStatus(HttpStatus.OK);
            return body;
        }

        body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        body.setMessage("登录凭证校验失败");
        return body;
    }
}
