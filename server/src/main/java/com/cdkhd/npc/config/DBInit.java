package com.cdkhd.npc.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.util.SysUtil;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

//数据初始化配置类
@Configuration
public class DBInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBInit.class);

    private AccountRoleRepository accountRoleRepository;
    private NpcMemberRoleRepository npcMemberRoleRepository;
    private PermissionRepository permissionRepository;
    private SystemRepository systemRepository;
    private MenuRepository menuRepository;
    private CommonDictRepository commonDictRepository;
    private Environment env;
    private AreaRepository areaRepository;
    private AccountRepository accountRepository;
    private GovernmentUserRepository governmentUserRepository;
    private LoginUPRepository loginUPRepository;
    private BackgroundAdminRepository backgroundAdminRepository;
    private SystemSettingRepository systemSettingRepository;
    private SessionRepository sessionRepository;
    private PerformanceTypeRepository performanceTypeRepository;

    private WeChatMenuRepository weChatMenuRepository;
    private WeChatAccessTokenRepository weChatAccessTokenRepository;
    private RestTemplate restTemplate;

    private final String CURRENT_APPID;
    private final String CURRENT_APPSECRET;
    private final String REDIRECT_URL;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public DBInit(AccountRoleRepository accountRoleRepository, NpcMemberRoleRepository npcMemberRoleRepository, PermissionRepository permissionRepository, SystemRepository systemRepository, MenuRepository menuRepository, CommonDictRepository commonDictRepository, Environment env, AreaRepository areaRepository, AccountRepository accountRepository, LoginUPRepository loginUPRepository, BackgroundAdminRepository backgroundAdminRepository, SystemSettingRepository systemSettingRepository, SessionRepository sessionRepository, PerformanceTypeRepository performanceTypeRepository, WeChatMenuRepository weChatMenuRepository, WeChatAccessTokenRepository weChatAccessTokenRepository, RestTemplate restTemplate, PasswordEncoder passwordEncoder) {
        this.accountRoleRepository = accountRoleRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.permissionRepository = permissionRepository;
        this.systemRepository = systemRepository;
        this.menuRepository = menuRepository;
        this.commonDictRepository = commonDictRepository;
        this.env = env;
        this.areaRepository = areaRepository;
        this.accountRepository = accountRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.loginUPRepository = loginUPRepository;
        this.backgroundAdminRepository = backgroundAdminRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.sessionRepository = sessionRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.weChatMenuRepository = weChatMenuRepository;
        this.weChatAccessTokenRepository = weChatAccessTokenRepository;
        this.restTemplate = restTemplate;
        CURRENT_APPID = env.getProperty("service_app.appid");
        CURRENT_APPSECRET = env.getProperty("service_app.appsecret");
        REDIRECT_URL = env.getProperty("service_app.redirect_url");
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        initRole();
        initPermission();
        initSystem();
        initMenu();
        initCommonDict();
        mapRoleAndPermission();
        mapPermissionMenu();
        mapRoleAndSystems();
//      mapMenuSystem();
        initArea();
        initAccount();
        initBackgroundAdmin();
        initSession();
        initSystemSetting();
        initPerformanceType();
        insertWeichatMenu();
    }

    private void initPerformanceType() {
        //初始化AccountRole
        String areaName = env.getProperty("npc_base_info.area");
        Area area = areaRepository.findByName(areaName);
        for (PerformanceTypeEnum performanceTypeEnum : PerformanceTypeEnum.values()) {
            PerformanceType performanceType = performanceTypeRepository.findByNameAndLevelAndAreaUidAndStatusAndIsDelFalseAndIsDefaultIsTrue(performanceTypeEnum.getValue(),LevelEnum.AREA.getValue(),area.getUid(),StatusEnum.ENABLED.getValue());
            if (performanceType == null) {
                Integer maxSequence = performanceTypeRepository.findMaxSequenceByLevelAndAreaUid(LevelEnum.AREA.getValue(), area.getUid());
                performanceType = new PerformanceType();
                performanceType.setSequence(maxSequence==null ? 1: maxSequence + 1);
                performanceType.setName(performanceTypeEnum.getValue());
                performanceType.setRemark("初始化数据，不可删除");
                performanceType.setArea(area);
                performanceType.setLevel(LevelEnum.AREA.getValue());
                performanceType.setIsDel(false);
                performanceType.setStatus(StatusEnum.ENABLED.getValue());
                performanceType.setIsDefault(true);
                performanceTypeRepository.saveAndFlush(performanceType);
            }
        }
    }

    private void initSystemSetting() {
        String areaName = env.getProperty("npc_base_info.area");
        Area area = areaRepository.findByName(areaName);
        SystemSetting systemSetting = systemSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(),area.getUid());
        if (systemSetting == null){
            systemSetting = new SystemSetting();
            systemSetting.setLevel(LevelEnum.AREA.getValue());
            systemSetting.setArea(area);
            systemSettingRepository.saveAndFlush(systemSetting);
        }
    }

    private void initSession() {
        String areaName = env.getProperty("npc_base_info.area");
        Area area = areaRepository.findByName(areaName);
        Session session = sessionRepository.findByAreaUidAndLevelAndStartDateIsNullAndEndDateIsNull(area.getUid(),LevelEnum.AREA.getValue());
        if (session == null){
            session = new Session();
            session.setArea(area);
            session.setLevel(LevelEnum.AREA.getValue());
            session.setName("其他");
            session.setRemark("其他情况");
            sessionRepository.saveAndFlush(session);
        }
    }

    private void initAccount() {
        //初始化账号信息
        String username = env.getProperty("npc_base_info.user.name");
        String mobile = env.getProperty("npc_base_info.user.mobile");
        String defaultRawPwd = env.getProperty("account.password"); //获取默认的明文密码
        initOneAccount(username, mobile, defaultRawPwd);

        //为公司初始化账号信息
        username = env.getProperty("npc_base_info.cdkhd.name");
        mobile = env.getProperty("npc_base_info.cdkhd.mobile");
        initOneAccount(username, mobile, defaultRawPwd);

        //为大数据平台初始化一个账号，默认用户名为“bigData”
        initOneAccount("bigData", "13133333333", defaultRawPwd);
    }

    private void initBackgroundAdmin() {
        String areaName = env.getProperty("npc_base_info.area");
        Area area = areaRepository.findByName(areaName);
        String username = env.getProperty("npc_base_info.user.name");
        Account account = accountRepository.findByUsername(username);
        //初始化后台管理员账号
        BackgroundAdmin backgroundAdmin = backgroundAdminRepository.findByAccountUsername(username);
        if (backgroundAdmin == null) {
            backgroundAdmin = new BackgroundAdmin();
            backgroundAdmin.setAccount(account);
            backgroundAdmin.setLevel(LevelEnum.AREA.getValue());
            backgroundAdmin.setArea(area);
            backgroundAdminRepository.saveAndFlush(backgroundAdmin);
        }

        username = env.getProperty("npc_base_info.cdkhd.name");
        account = accountRepository.findByUsername(username);
        //初始化后台管理员账号
        backgroundAdmin = backgroundAdminRepository.findByAccountUsername(username);
        if (backgroundAdmin == null) {
            backgroundAdmin = new BackgroundAdmin();
            backgroundAdmin.setAccount(account);
            backgroundAdmin.setLevel(LevelEnum.AREA.getValue());
            backgroundAdmin.setArea(area);
            backgroundAdminRepository.saveAndFlush(backgroundAdmin);
        }
        account = accountRepository.findByUsername("bigData");
        backgroundAdmin = backgroundAdminRepository.findByAccountUsername("bigData");
        if (backgroundAdmin == null) {
            backgroundAdmin = new BackgroundAdmin();
            backgroundAdmin.setAccount(account);
            backgroundAdmin.setLevel(LevelEnum.AREA.getValue());
            backgroundAdmin.setArea(area);
            backgroundAdminRepository.saveAndFlush(backgroundAdmin);
        }
    }

    private void initArea() {
        String areaName = env.getProperty("npc_base_info.area");
        Area area = areaRepository.findByName(areaName);
        if (area == null) {
            area = new Area();
            area.setName(areaName);
            area.setRemark(areaName);
            areaRepository.saveAndFlush(area);
        }
    }

    //初始化角色
    private void initRole() {
        //初始化AccountRole
        for (AccountRoleEnum accountRoleEnum : AccountRoleEnum.values()) {
            AccountRole accountRole = accountRoleRepository.findByKeyword(accountRoleEnum.getKeyword());
            if (accountRole == null) {
                accountRole = new AccountRole();
                accountRole.setKeyword(accountRoleEnum.getKeyword());
                accountRole.setName(accountRoleEnum.getName());
                accountRoleRepository.saveAndFlush(accountRole);
            }
        }

        //初始化NpcMemberRole
        for (NpcMemberRoleEnum npcMemberRoleEnum : NpcMemberRoleEnum.values()) {
            NpcMemberRole npcMemberRole = npcMemberRoleRepository.findByKeyword(npcMemberRoleEnum.getKeyword());
            if (npcMemberRole == null) {
                npcMemberRole = new NpcMemberRole();
                npcMemberRole.setKeyword(npcMemberRoleEnum.getKeyword());
                npcMemberRole.setName(npcMemberRoleEnum.getName());
                npcMemberRole.setIsMust(npcMemberRoleEnum.getIsMust());
                npcMemberRole.setSpecial(npcMemberRoleEnum.getSpecial());
                npcMemberRoleRepository.saveAndFlush(npcMemberRole);
            }
        }
    }

    //初始化权限
    private void initPermission() {
        //遍历permission枚举，初始化
        for (PermissionEnum permissionEnum : PermissionEnum.values()) {
            Permission permission = permissionRepository.findByKeyword(permissionEnum.getKeyword());
            if (permission == null) {
                permission = new Permission();
                permission.setKeyword(permissionEnum.getKeyword());
                permission.setName(permissionEnum.getName());
                permissionRepository.saveAndFlush(permission);
            }
        }
    }

    //初始化系统
    private void initSystem() {
        for (SystemEnum systemEnum : SystemEnum.values()) {
            Systems systems = systemRepository.findByName(systemEnum.getName());
            if (systems == null) {
                systems = new Systems();
                systems.setName(systemEnum.getName());
                systems.setDescription(systemEnum.getDescription());
                systems.setSvg(systemEnum.getSvg());
                systems.setUrl(systemEnum.getUrl());
                systems.setEnabled(true);
                systems.setKeyword(systemEnum.toString());
                systems.setImgUrl(systemEnum.getImgUrl());
                systems.setPagePath(systemEnum.getPagePath());
                systems.setMiniShow(systemEnum.getMiniShow());
                systemRepository.save(systems);
            }
        }
    }

    //初始化菜单
    private void initMenu() {
        //遍历Menu枚举，初始化菜单
        for (MenuEnum menuEnum : MenuEnum.values()) {
            Menu menu = menuRepository.findByKeyword(menuEnum.toString());
            if (menu == null) {
                menu = new Menu();
                menu.setName(menuEnum.getName());
                menu.setUrl(menuEnum.getUrl());
                menu.setIcon(menuEnum.getIcon());
                menu.setKeyword(menuEnum.toString());
                menu.setRoute(menuEnum.getRoute());
                menu.setType(menuEnum.getType());
                if (StringUtils.isNotEmpty(menuEnum.getParentId())) {
                    menu.setParent(menuRepository.findByKeyword(menuEnum.getParentId()));
                }
                menu.setSystems(systemRepository.findByKeyword(menuEnum.getSystem()));
                menuRepository.saveAndFlush(menu);
            }
        }
    }

    //为角色关联权限
    private void mapRoleAndPermission() {
        //选民
        AccountRole voter = accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword());
        Set<Permission> voterPermissions = voter.getPermissions();
        //代表之家
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));   //代表风采
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));   //我的意见
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));   //代表排名
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));   //各镇排名
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STREET_RANK.getKeyword()));   //街道排名
        voter.setPermissions(voterPermissions);
        accountRoleRepository.save(voter);

        //后台管理员
        AccountRole bgAdmin = accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword());
        Set<Permission> bgAdminPermissions = bgAdmin.getPermissions();
        //代表之家
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.HOMEPAGE.getKeyword()));//首页
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.ACCOUNT_MANAGE.getKeyword()));//账号管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NEWS_TYPE_MANAGE.getKeyword()));//新闻类型管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NEWS_MANAGE.getKeyword()));//新闻管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NOTICE_MANAGE.getKeyword()));//通知管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NPC_MANAGE.getKeyword()));//代表管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GROUP_MANAGE.getKeyword()));//代表小组管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.WORKSTATION_MANAGE.getKeyword()));//工作站管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.OPINION_MANAGE.getKeyword()));//选民意见管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STUDY_TYPE_MANAGE.getKeyword()));//学习类型管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STUDY_MANAGE.getKeyword()));//学习资料管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_TYPE.getKeyword()));//代表建议类型
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_MANAGE.getKeyword()));//代表建议管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_SUGGESTION_MANAGE.getKeyword()));//代表建议管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_COUNT.getKeyword()));//代表建议统计
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_TYPE.getKeyword()));//代表履职类型
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_MANAGE.getKeyword()));//代表履职管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_PERFORMANCE_MANAGE.getKeyword()));//代表履职管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_COUNT.getKeyword()));//代表履职统计
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_PERFORMANCE_COUNT.getKeyword()));//各镇履职统计
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_MANAGE.getKeyword()));//镇管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VILLAGE_MANAGE.getKeyword()));//村管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERMISSION_MANAGE.getKeyword()));//代表权限管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NEWS_AUDITOR.getKeyword()));//新闻审核人设置
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SESSION_MANAGE.getKeyword()));//届期管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SYSTEM_SETTING.getKeyword()));//系统设置



        //建议办理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NPC_HOMEPAGE_DEAL.getKeyword()));//建议办理首页
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOVERNMENT_MANAGE.getKeyword()));//政府管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_TYPE_DEAL.getKeyword()));//建议类型管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_DEAL.getKeyword()));//代表建议管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_RECEIVER.getKeyword()));//建议接收人设置

        bgAdmin.setPermissions(bgAdminPermissions);
        accountRoleRepository.save(bgAdmin);

        //普通代表
        NpcMemberRole member = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.MEMBER.getKeyword());
        Set<Permission> memberPermissions = member.getPermissions();
        //代表之家
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));//代表风采
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));//我的意见
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));//收到意见
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));//我的建议
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));//我的履职
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));//接收通知
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));//代表排名
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));//各镇排名
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STREET_RANK.getKeyword()));   //街道排名
        //建议办理
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_MY_SUGGESTION.getKeyword()));   //我的建议
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_OTHERS_SUGGESTION.getKeyword()));   //他人建议
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_SECONDED_SUGGESTION.getKeyword()));   //附议建议
        member.setPermissions(memberPermissions);
        npcMemberRoleRepository.save(member);

        //人大主席（暂时与普通代表权限相同）
        NpcMemberRole chairman = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.CHAIRMAN.getKeyword());
        Set<Permission> chairmanPermissions = chairman.getPermissions();
        //代表之家
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));//代表风采
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));//我的意见
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));//收到意见
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));//我的建议
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));//我的履职
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));//接收通知
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));//代表排名
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));//各镇排名
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STREET_RANK.getKeyword()));   //街道排名
        //建议办理
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_MY_SUGGESTION.getKeyword()));   //我的建议
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_OTHERS_SUGGESTION.getKeyword()));   //他人建议
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_SECONDED_SUGGESTION.getKeyword()));   //附议建议
        chairman.setPermissions(chairmanPermissions);
        npcMemberRoleRepository.save(chairman);

        //特殊人员
        NpcMemberRole specialMan = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.SPECIAL_MAN.getKeyword());
        Set<Permission> specialPermissions = specialMan.getPermissions();
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));//代表风采
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));//我的意见
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));//接收通知
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));//代表排名
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));//各镇排名
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STREET_RANK.getKeyword()));   //街道排名
        specialMan.setPermissions(specialPermissions);
        npcMemberRoleRepository.save(specialMan);

        //新闻审核人
        NpcMemberRole newsAuditor = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword());
        Set<Permission> newAuditorPermissions = newsAuditor.getPermissions();
        newAuditorPermissions.add(permissionRepository.findByKeyword(PermissionEnum.AUDIT_NEWS.getKeyword()));//新闻审核
        newsAuditor.setPermissions(newAuditorPermissions);
        npcMemberRoleRepository.save(newsAuditor);

        //履职审核人
        NpcMemberRole performanceAuditor = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());
        Set<Permission> performanceAuditorPermissions = performanceAuditor.getPermissions();
        performanceAuditorPermissions.add(permissionRepository.findByKeyword(PermissionEnum.AUDIT_PERFORMANCE.getKeyword()));//履职审核
        performanceAuditor.setPermissions(performanceAuditorPermissions);
        npcMemberRoleRepository.save(performanceAuditor);

        //履职总审核人
        NpcMemberRole performanceGeneralAuditor = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword());
        Set<Permission> performanceGeneralPermissions = performanceGeneralAuditor.getPermissions();
        performanceGeneralPermissions.add(permissionRepository.findByKeyword(PermissionEnum.AUDIT_PERFORMANCE.getKeyword()));//履职审核
        performanceGeneralAuditor.setPermissions(performanceGeneralPermissions);
        npcMemberRoleRepository.save(performanceGeneralAuditor);


        //建议接受人
        NpcMemberRole suggestionAuditor = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.SUGGESTION_RECEIVER.getKeyword());
        Set<Permission> suggestionAuditorPermissions = suggestionAuditor.getPermissions();
        //代表之家
        suggestionAuditorPermissions.add(permissionRepository.findByKeyword(PermissionEnum.AUDIT_SUGGESTION.getKeyword()));//建议审核
        //建议办理
        suggestionAuditorPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_AUDIT_SUGGESTION.getKeyword()));//建议审核
        suggestionAuditor.setPermissions(suggestionAuditorPermissions);
        npcMemberRoleRepository.save(suggestionAuditor);

        //通知公告审核人
        NpcMemberRole noticeAuditor = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword());
        Set<Permission> noticeAuditorPermissions = noticeAuditor.getPermissions();
        noticeAuditorPermissions.add(permissionRepository.findByKeyword(PermissionEnum.AUDIT_NOTICE.getKeyword()));//通知审核
        noticeAuditor.setPermissions(noticeAuditorPermissions);
        npcMemberRoleRepository.save(noticeAuditor);

        //政府人员
        AccountRole govUser = accountRoleRepository.findByKeyword(AccountRoleEnum.GOVERNMENT.getKeyword());
        Set<Permission> govPermissions = govUser.getPermissions();
        //小程序
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.DEAL_CONVEY_SUGGESTIONS.getKeyword()));   //转办建议
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_DELAY_SUGGESTION.getKeyword()));   //延期建议
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_ADJUST_SUGGESTION.getKeyword()));   //调整单位
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_URGE_SUGGESTION.getKeyword()));   //查看建议情况
        //后台
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_HOMEPAGE_DEAL.getKeyword()));   //首页
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_WAIT_CONVEY.getKeyword()));   //待转办
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_ADJUST_CONVEY.getKeyword()));   //调整单位
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_ADJUST_DELAY.getKeyword()));   //申请延期
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_DEALING.getKeyword()));   //办理中
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_FINISHED.getKeyword()));   //办完的建议
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_COMPLETED.getKeyword()));   //办结的建议
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_MANAGE.getKeyword()));   //办理单位管理
        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_SETTING.getKeyword()));   //建议设置
//        govPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GOV_COUNT.getKeyword()));   //统计
        govUser.setPermissions(govPermissions);
        accountRoleRepository.save(govUser);

        //办理单位
        AccountRole unitUser = accountRoleRepository.findByKeyword(AccountRoleEnum.UNIT.getKeyword());
        Set<Permission> unitPermissions = unitUser.getPermissions();
        //小程序
        unitPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_WAIT_DEAL_SUGGESTION.getKeyword()));   //单位待办理
        unitPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_DEAL_SUGGESTION.getKeyword()));   //单位办理建议
        //后台
        unitPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_HOMEPAGE_DEAL.getKeyword()));   //首页
        unitPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_WAIT_DEAL.getKeyword()));   //办理单待办理列表
        unitPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_DEALING.getKeyword()));   //办理单位办理中列表
        unitPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_DEAL_DONE.getKeyword()));   //办理单位办理完成列表
        unitPermissions.add(permissionRepository.findByKeyword(PermissionEnum.UNIT_DEAL_COMPLETED.getKeyword()));   //办理单位办结列表
        unitUser.setPermissions(unitPermissions);
        accountRoleRepository.save(unitUser);

    }

    //为权限关联菜单
    private void mapPermissionMenu() {

        //代表风采
        Menu menu = menuRepository.findByKeyword(MenuEnum.MEMBER_INFO.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //查看联络点
        menu = menuRepository.findByKeyword(MenuEnum.WORK_STATION.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //我的意见
        menu = menuRepository.findByKeyword(MenuEnum.MY_OPINION.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //我的建议
        menu = menuRepository.findByKeyword(MenuEnum.MY_SUGGESTION.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //我的履职
        menu = menuRepository.findByKeyword(MenuEnum.MY_PERFORMANCE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //收到的意见
        menu = menuRepository.findByKeyword(MenuEnum.RECEIVE_OPINION.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //接收通知
        menu = menuRepository.findByKeyword(MenuEnum.NOTIFICATION_INFO.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表排名
        menu = menuRepository.findByKeyword(MenuEnum.MEMBER_RANK.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //各镇排名
        menu = menuRepository.findByKeyword(MenuEnum.TOWN_RANK.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //各镇排名
        menu = menuRepository.findByKeyword(MenuEnum.STREET_RANK.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.STREET_RANK.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核建议
        menu = menuRepository.findByKeyword(MenuEnum.AUDIT_SUGGESTION.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_SUGGESTION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核履职
        menu = menuRepository.findByKeyword(MenuEnum.AUDIT_PERFORMANCE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_PERFORMANCE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核新闻
        menu = menuRepository.findByKeyword(MenuEnum.AUDIT_NEWS.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_NEWS.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核通知
        menu = menuRepository.findByKeyword(MenuEnum.AUDIT_NOTICE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_NOTICE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //建议办理
        Permission permission = permissionRepository.findByKeyword(PermissionEnum.DEAL_MY_SUGGESTION.getKeyword());//我的建议权限
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_DRAFT.toString());//草稿
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_COMMITTED.toString());//已提交
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_DONE.toString());//已办完
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_COMPLETED.toString());//已办结
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);

        //他人建议
        //我能附议的
        menu = menuRepository.findByKeyword(MenuEnum.OTHERS_SUGGESTIONS.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.DEAL_OTHERS_SUGGESTION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //附议建议
        permission = permissionRepository.findByKeyword(PermissionEnum.DEAL_SECONDED_SUGGESTION.getKeyword());//附议建议
        menu = menuRepository.findByKeyword(MenuEnum.SECONDED_SUGGESTIONS.toString());//我附议的
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.SECONDED_SUGGESTIONS_COMPLETED.toString());//附议办结的
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);

        //审核建议
        permission = permissionRepository.findByKeyword(PermissionEnum.DEAL_AUDIT_SUGGESTION.getKeyword());//审核建议
        menu = menuRepository.findByKeyword(MenuEnum.WAIT_AUDIT_SUGGESTIONS.toString());//待审核建议
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.AUDIT_PASS_SUGGESTIONS.toString());//审核通过的建议
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.AUDIT_FAILED_SUGGESTIONS.toString());//审核失败的建议
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);

        //转办建议
        permission = permissionRepository.findByKeyword(PermissionEnum.DEAL_CONVEY_SUGGESTIONS.getKeyword());//转办建议
        menu = menuRepository.findByKeyword(MenuEnum.WAIT_CONVEY_SUGGESTIONS.toString());//待转办
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
//        menu = menuRepository.findByKeyword(MenuEnum.CONVEYED_SUGGESTIONS.toString());//已转办
//        menu.setPermission(permission);
//        menuRepository.saveAndFlush(menu);

        //延期申请
        menu = menuRepository.findByKeyword(MenuEnum.APPLY_DELAY_SUGGESTIONS.toString());//延期申请
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_DELAY_SUGGESTION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //调整单位
        menu = menuRepository.findByKeyword(MenuEnum.APPLY_ADJUST_SUGGESTIONS.toString());//调整单位
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_ADJUST_SUGGESTION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //查看建议情况
        permission = permissionRepository.findByKeyword(PermissionEnum.GOV_URGE_SUGGESTION.getKeyword());//查看建议办理情况
        menu = menuRepository.findByKeyword(MenuEnum.GOV_DEALING_SUGGESTIONS.toString());//办理中
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.GOV_FINISHED_SUGGESTIONS.toString());//已办完
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.GOV_COMPLETED_SUGGESTIONS.toString());//已办结
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);

        //单位待办理
        permission = permissionRepository.findByKeyword(PermissionEnum.UNIT_WAIT_DEAL_SUGGESTION.getKeyword());//单位待办理
        menu = menuRepository.findByKeyword(MenuEnum.WAIT_DEAL_SUGGESTIONS.toString());//单位待办理
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);

        //转办建议
        permission = permissionRepository.findByKeyword(PermissionEnum.UNIT_DEAL_SUGGESTION.getKeyword());//转办建议
        menu = menuRepository.findByKeyword(MenuEnum.DEALING_SUGGESTIONS.toString());//办理中
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.DEAL_DONE_SUGGESTIONS.toString());//已办完
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);
        menu = menuRepository.findByKeyword(MenuEnum.DEAL_COMPLETED_SUGGESTIONS.toString());//已办结
        menu.setPermission(permission);
        menuRepository.saveAndFlush(menu);

        //****************后台菜单

        //首页
        menu = menuRepository.findByKeyword(MenuEnum.HOMEPAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.HOMEPAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //账号管理
        menu = menuRepository.findByKeyword(MenuEnum.ACCOUNT_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.ACCOUNT_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //新闻管理
        menu = menuRepository.findByKeyword(MenuEnum.NEWS_TYPE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NEWS_TYPE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //新闻管理
        menu = menuRepository.findByKeyword(MenuEnum.NEWS_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NEWS_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //通知管理
        menu = menuRepository.findByKeyword(MenuEnum.NOTICE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NOTICE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表管理
        menu = menuRepository.findByKeyword(MenuEnum.NPC_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NPC_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //小组管理
        menu = menuRepository.findByKeyword(MenuEnum.NPC_MEMBER_GROUP.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GROUP_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //联络点管理
        menu = menuRepository.findByKeyword(MenuEnum.WORKSTATION_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.WORKSTATION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //选民意见管理
        menu = menuRepository.findByKeyword(MenuEnum.OPINION_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.OPINION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //学习类型管理
        menu = menuRepository.findByKeyword(MenuEnum.STUDY_TYPE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.STUDY_TYPE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //学习资料管理
        menu = menuRepository.findByKeyword(MenuEnum.STUDY_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.STUDY_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表建议类型
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_TYPE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_TYPE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表建议管理
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //各镇代表建议管理
        menu = menuRepository.findByKeyword(MenuEnum.TOWN_SUGGESTION_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.TOWN_SUGGESTION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表履职类型
        menu = menuRepository.findByKeyword(MenuEnum.PERFORMANCE_TYPE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_TYPE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表履职管理
        menu = menuRepository.findByKeyword(MenuEnum.PERFORMANCE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //各镇代表履职管理
        menu = menuRepository.findByKeyword(MenuEnum.TOWN_PERFORMANCE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.TOWN_PERFORMANCE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表建议统计
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_COUNT.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_COUNT.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表履职统计
        menu = menuRepository.findByKeyword(MenuEnum.PERFORMANCE_COUNT.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_COUNT.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表权限管理
        menu = menuRepository.findByKeyword(MenuEnum.NEWS_AUDITOR.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NEWS_AUDITOR.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表权限管理
        menu = menuRepository.findByKeyword(MenuEnum.PERMISSION_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERMISSION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //镇管理
        menu = menuRepository.findByKeyword(MenuEnum.TOWN_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.TOWN_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //村管理
        menu = menuRepository.findByKeyword(MenuEnum.VILLAGE_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.VILLAGE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //届期管理
        menu = menuRepository.findByKeyword(MenuEnum.SESSION_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SESSION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //系统设置
        menu = menuRepository.findByKeyword(MenuEnum.SYSTEM_SETTING.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SYSTEM_SETTING.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //建议办理
        //后台管理员
        //首页
        menu = menuRepository.findByKeyword(MenuEnum.NPC_HOMEPAGE_DEAL.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NPC_HOMEPAGE_DEAL.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //政府管理
        menu = menuRepository.findByKeyword(MenuEnum.GOVERNMENT_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOVERNMENT_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //建议类型管理
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_TYPE_DEAL.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_TYPE_DEAL.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表建议管理
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_DEAL.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_DEAL.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //建议接收人设置
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_RECEIVER_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_RECEIVER.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //政府
        //首页
        menu = menuRepository.findByKeyword(MenuEnum.GOV_HOMEPAGE_DEAL.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_HOMEPAGE_DEAL.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //待转办
        menu = menuRepository.findByKeyword(MenuEnum.GOV_WAIT_CONVEY.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_WAIT_CONVEY.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //调整单位
        menu = menuRepository.findByKeyword(MenuEnum.GOV_ADJUST_CONVEY.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_ADJUST_CONVEY.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //申请延期
        menu = menuRepository.findByKeyword(MenuEnum.GOV_ADJUST_DELAY.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_ADJUST_DELAY.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //办理中
        menu = menuRepository.findByKeyword(MenuEnum.GOV_DEALING.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_DEALING.getKeyword()));
        menuRepository.saveAndFlush(menu);
        //已完成
        menu = menuRepository.findByKeyword(MenuEnum.GOV_FINISHED.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_FINISHED.getKeyword()));
        menuRepository.saveAndFlush(menu);
        //已办结
        menu = menuRepository.findByKeyword(MenuEnum.GOV_COMPLETED.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_COMPLETED.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //办理单位管理
        menu = menuRepository.findByKeyword(MenuEnum.UNIT_MANAGE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.UNIT_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //建议设置
        menu = menuRepository.findByKeyword(MenuEnum.SUGGESTION_SETTING.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_SETTING.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //统计
//        menu = menuRepository.findByKeyword(MenuEnum.GOV_COUNT.toString());
//        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GOV_COUNT.getKeyword()));
//        menuRepository.saveAndFlush(menu);

        //首页
        menu = menuRepository.findByKeyword(MenuEnum.UNIT_HOMEPAGE_DEAL.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.UNIT_HOMEPAGE_DEAL.getKeyword()));
        menuRepository.saveAndFlush(menu);


        //办理单待办理列表
        menu = menuRepository.findByKeyword(MenuEnum.UNIT_WAIT_DEAL.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.UNIT_WAIT_DEAL.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //办理单位办理中列表
        menu = menuRepository.findByKeyword(MenuEnum.UNIT_DEALING.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.UNIT_DEALING.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //办理单位办理完成列表
        menu = menuRepository.findByKeyword(MenuEnum.UNIT_DEAL_DONE.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.UNIT_DEAL_DONE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //办理单位办结列表
        menu = menuRepository.findByKeyword(MenuEnum.UNIT_DEAL_COMPLETED.toString());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.UNIT_DEAL_COMPLETED.getKeyword()));
        menuRepository.saveAndFlush(menu);

    }

    //为菜单关联系统
//    private void mapMenuSystem() {
//        for (Menu menu : menuRepository.findAll()) {
//            menu.setSystems(systemRepository.findByName("代表之家系统"));
//            menuRepository.save(menu);
//        }
//    }

    //初始化码表：民族，政治面貌，受教育程度
    private void initCommonDict() {
        List<CommonDict> nations = commonDictRepository.findByTypeAndIsDelFalse(CommonDictTypeEnum.NATION.getValue());
        List<CommonDict> educations = commonDictRepository.findByTypeAndIsDelFalse(CommonDictTypeEnum.EDUCATION.getValue());
        List<CommonDict> politics = commonDictRepository.findByTypeAndIsDelFalse(CommonDictTypeEnum.POLITIC.getValue());
        List<CommonDict> jobs = commonDictRepository.findByTypeAndIsDelFalse(CommonDictTypeEnum.JOBS.getValue());

        //添加民族
        if (nations.size() == 0){
            CommonDict commonDict01 = new CommonDict();
            commonDict01.setCode("01");
            commonDict01.setName("汉族");
            commonDict01.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict01.setTypeName("民族");
            nations.add(commonDict01);

            CommonDict commonDict02 = new CommonDict();
            commonDict02.setCode("02");
            commonDict02.setName("蒙古族");
            commonDict02.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict02.setTypeName("民族");
            nations.add(commonDict02);

            CommonDict commonDict03 = new CommonDict();
            commonDict03.setCode("03");
            commonDict03.setName("回族");
            commonDict03.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict03.setTypeName("民族");
            nations.add(commonDict03);

            CommonDict commonDict04 = new CommonDict();
            commonDict04.setCode("04");
            commonDict04.setName("藏族");
            commonDict04.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict04.setTypeName("民族");
            nations.add(commonDict04);

            CommonDict commonDict05 = new CommonDict();
            commonDict05.setCode("05");
            commonDict05.setName("维吾尔族");
            commonDict05.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict05.setTypeName("民族");
            nations.add(commonDict05);

            CommonDict commonDict06 = new CommonDict();
            commonDict06.setCode("06");
            commonDict06.setName("苗族");
            commonDict06.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict06.setTypeName("民族");
            nations.add(commonDict06);

            CommonDict commonDict07 = new CommonDict();
            commonDict07.setCode("07");
            commonDict07.setName("彝族");
            commonDict07.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict07.setTypeName("民族");
            nations.add(commonDict07);

            CommonDict commonDict08 = new CommonDict();
            commonDict08.setCode("08");
            commonDict08.setName("壮族");
            commonDict08.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict08.setTypeName("民族");
            nations.add(commonDict08);

            CommonDict commonDict09 = new CommonDict();
            commonDict09.setCode("09");
            commonDict09.setName("布依族");
            commonDict09.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict09.setTypeName("民族");
            nations.add(commonDict09);

            CommonDict commonDict10 = new CommonDict();
            commonDict10.setCode("10");
            commonDict10.setName("朝鲜族");
            commonDict10.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict10.setTypeName("民族");
            nations.add(commonDict10);

            CommonDict commonDict11 = new CommonDict();
            commonDict11.setCode("11");
            commonDict11.setName("满族");
            commonDict11.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict11.setTypeName("民族");
            nations.add(commonDict11);

            CommonDict commonDict12 = new CommonDict();
            commonDict12.setCode("12");
            commonDict12.setName("侗族");
            commonDict12.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict12.setTypeName("民族");
            nations.add(commonDict12);

            CommonDict commonDict13 = new CommonDict();
            commonDict13.setCode("13");
            commonDict13.setName("瑶族");
            commonDict13.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict13.setTypeName("民族");
            nations.add(commonDict13);

            CommonDict commonDict14 = new CommonDict();
            commonDict14.setCode("14");
            commonDict14.setName("白族");
            commonDict14.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict14.setTypeName("民族");
            nations.add(commonDict14);

            CommonDict commonDict15 = new CommonDict();
            commonDict15.setCode("15");
            commonDict15.setName("土家族");
            commonDict15.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict15.setTypeName("民族");
            nations.add(commonDict15);

            CommonDict commonDict16 = new CommonDict();
            commonDict16.setCode("16");
            commonDict16.setName("哈尼族");
            commonDict16.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict16.setTypeName("民族");
            nations.add(commonDict16);

            CommonDict commonDict17 = new CommonDict();
            commonDict17.setCode("17");
            commonDict17.setName("哈萨克族");
            commonDict17.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict17.setTypeName("民族");
            nations.add(commonDict17);

            CommonDict commonDict18 = new CommonDict();
            commonDict18.setCode("18");
            commonDict18.setName("傣族");
            commonDict18.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict18.setTypeName("民族");
            nations.add(commonDict18);

            CommonDict commonDict19 = new CommonDict();
            commonDict19.setCode("19");
            commonDict19.setName("黎族");
            commonDict19.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict19.setTypeName("民族");
            nations.add(commonDict19);

            CommonDict commonDict20 = new CommonDict();
            commonDict20.setCode("20");
            commonDict20.setName("傈傈族");
            commonDict20.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict20.setTypeName("民族");
            nations.add(commonDict20);

            CommonDict commonDict21 = new CommonDict();
            commonDict21.setCode("21");
            commonDict21.setName("佤族");
            commonDict21.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict21.setTypeName("民族");
            nations.add(commonDict21);

            CommonDict commonDict22 = new CommonDict();
            commonDict22.setCode("22");
            commonDict22.setName("畲族");
            commonDict22.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict22.setTypeName("民族");
            nations.add(commonDict22);

            CommonDict commonDict23 = new CommonDict();
            commonDict23.setCode("23");
            commonDict23.setName("高山族");
            commonDict23.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict23.setTypeName("民族");
            nations.add(commonDict23);

            CommonDict commonDict24 = new CommonDict();
            commonDict24.setCode("24");
            commonDict24.setName("拉祜族");
            commonDict24.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict24.setTypeName("民族");
            nations.add(commonDict24);

            CommonDict commonDict25 = new CommonDict();
            commonDict25.setCode("25");
            commonDict25.setName("水族");
            commonDict25.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict25.setTypeName("民族");
            nations.add(commonDict25);

            CommonDict commonDict26 = new CommonDict();
            commonDict26.setCode("26");
            commonDict26.setName("东乡族");
            commonDict26.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict26.setTypeName("民族");
            nations.add(commonDict26);

            CommonDict commonDict27 = new CommonDict();
            commonDict27.setCode("27");
            commonDict27.setName("纳西族");
            commonDict27.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict27.setTypeName("民族");
            nations.add(commonDict27);

            CommonDict commonDict28 = new CommonDict();
            commonDict28.setCode("28");
            commonDict28.setName("景颇族");
            commonDict28.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict28.setTypeName("民族");
            nations.add(commonDict28);

            CommonDict commonDict29 = new CommonDict();
            commonDict29.setCode("29");
            commonDict29.setName("柯尔克孜族");
            commonDict29.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict29.setTypeName("民族");
            nations.add(commonDict29);

            CommonDict commonDict30 = new CommonDict();
            commonDict30.setCode("30");
            commonDict30.setName("土族");
            commonDict30.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict30.setTypeName("民族");
            nations.add(commonDict30);

            CommonDict commonDict31 = new CommonDict();
            commonDict31.setCode("31");
            commonDict31.setName("达翰尔族");
            commonDict31.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict31.setTypeName("民族");
            nations.add(commonDict31);

            CommonDict commonDict32 = new CommonDict();
            commonDict32.setCode("32");
            commonDict32.setName("仫佬族");
            commonDict32.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict32.setTypeName("民族");
            nations.add(commonDict32);

            CommonDict commonDict33 = new CommonDict();
            commonDict33.setCode("33");
            commonDict33.setName("羌族");
            commonDict33.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict33.setTypeName("民族");
            nations.add(commonDict33);

            CommonDict commonDict34 = new CommonDict();
            commonDict34.setCode("34");
            commonDict34.setName("布朗族");
            commonDict34.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict34.setTypeName("民族");
            nations.add(commonDict34);

            CommonDict commonDict35 = new CommonDict();
            commonDict35.setCode("35");
            commonDict35.setName("撒拉族");
            commonDict35.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict35.setTypeName("民族");
            nations.add(commonDict35);

            CommonDict commonDict36 = new CommonDict();
            commonDict36.setCode("36");
            commonDict36.setName("毛南族");
            commonDict36.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict36.setTypeName("民族");
            nations.add(commonDict36);

            CommonDict commonDict37 = new CommonDict();
            commonDict37.setCode("37");
            commonDict37.setName("仡佬族");
            commonDict37.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict37.setTypeName("民族");
            nations.add(commonDict37);

            CommonDict commonDict38 = new CommonDict();
            commonDict38.setCode("38");
            commonDict38.setName("锡伯族");
            commonDict38.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict38.setTypeName("民族");
            nations.add(commonDict38);

            CommonDict commonDict39 = new CommonDict();
            commonDict39.setCode("39");
            commonDict39.setName("阿昌族");
            commonDict39.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict39.setTypeName("民族");
            nations.add(commonDict39);

            CommonDict commonDict40 = new CommonDict();
            commonDict40.setCode("40");
            commonDict40.setName("普米族");
            commonDict40.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict40.setTypeName("民族");
            nations.add(commonDict40);

            CommonDict commonDict41 = new CommonDict();
            commonDict41.setCode("41");
            commonDict41.setName("塔吉克族");
            commonDict41.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict41.setTypeName("民族");
            nations.add(commonDict41);

            CommonDict commonDict42 = new CommonDict();
            commonDict42.setCode("42");
            commonDict42.setName("怒族");
            commonDict42.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict42.setTypeName("民族");
            nations.add(commonDict42);

            CommonDict commonDict43 = new CommonDict();
            commonDict43.setCode("43");
            commonDict43.setName("乌兹别克族");
            commonDict43.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict43.setTypeName("民族");
            nations.add(commonDict43);

            CommonDict commonDict44 = new CommonDict();
            commonDict44.setCode("44");
            commonDict44.setName("俄罗斯族");
            commonDict44.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict44.setTypeName("民族");
            nations.add(commonDict44);

            CommonDict commonDict45 = new CommonDict();
            commonDict45.setCode("45");
            commonDict45.setName("鄂温克族");
            commonDict45.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict45.setTypeName("民族");
            nations.add(commonDict45);

            CommonDict commonDict46 = new CommonDict();
            commonDict46.setCode("46");
            commonDict46.setName("德昂族");
            commonDict46.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict46.setTypeName("民族");
            nations.add(commonDict46);

            CommonDict commonDict47 = new CommonDict();
            commonDict47.setCode("47");
            commonDict47.setName("保安族");
            commonDict47.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict47.setTypeName("民族");
            nations.add(commonDict47);

            CommonDict commonDict48 = new CommonDict();
            commonDict48.setCode("48");
            commonDict48.setName("裕固族");
            commonDict48.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict48.setTypeName("民族");
            nations.add(commonDict48);

            CommonDict commonDict49 = new CommonDict();
            commonDict49.setCode("49");
            commonDict49.setName("京族");
            commonDict49.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict49.setTypeName("民族");
            nations.add(commonDict49);

            CommonDict commonDict50 = new CommonDict();
            commonDict50.setCode("50");
            commonDict50.setName("塔塔尔族");
            commonDict50.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict50.setTypeName("民族");
            nations.add(commonDict50);

            CommonDict commonDict51 = new CommonDict();
            commonDict51.setCode("51");
            commonDict51.setName("独龙族");
            commonDict51.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict51.setTypeName("民族");
            nations.add(commonDict51);

            CommonDict commonDict52 = new CommonDict();
            commonDict52.setCode("52");
            commonDict52.setName("鄂伦春族");
            commonDict52.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict52.setTypeName("民族");
            nations.add(commonDict52);

            CommonDict commonDict53 = new CommonDict();
            commonDict53.setCode("53");
            commonDict53.setName("郝哲族");
            commonDict53.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict53.setTypeName("民族");
            nations.add(commonDict53);

            CommonDict commonDict54 = new CommonDict();
            commonDict54.setCode("54");
            commonDict54.setName("门巴族");
            commonDict54.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict54.setTypeName("民族");
            nations.add(commonDict54);

            CommonDict commonDict55 = new CommonDict();
            commonDict55.setCode("55");
            commonDict55.setName("珞巴族");
            commonDict55.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict55.setTypeName("民族");
            nations.add(commonDict55);

            CommonDict commonDict56 = new CommonDict();
            commonDict56.setCode("56");
            commonDict56.setName("基诺族");
            commonDict56.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict56.setTypeName("民族");
            nations.add(commonDict56);

            CommonDict commonDict57 = new CommonDict();
            commonDict57.setCode("57");
            commonDict57.setName("其他");
            commonDict57.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict57.setTypeName("民族");
            nations.add(commonDict57);

            CommonDict commonDict58 = new CommonDict();
            commonDict58.setCode("58");
            commonDict58.setName("外国血统");
            commonDict58.setType(CommonDictTypeEnum.NATION.getValue());
            commonDict58.setTypeName("民族");
            nations.add(commonDict58);

            //保存
            commonDictRepository.saveAll(nations);
        }

        //添加政治面貌
        if (politics.size() == 0){
            CommonDict commonDict1 = new CommonDict();
            commonDict1.setCode("01");
            commonDict1.setName("群众");
            commonDict1.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict1.setTypeName("政治面貌");
            politics.add(commonDict1);

            CommonDict commonDict02 = new CommonDict();
            commonDict02.setCode("02");
            commonDict02.setName("中共党员");
            commonDict02.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict02.setTypeName("政治面貌");
            politics.add(commonDict02);

            CommonDict commonDict03 = new CommonDict();
            commonDict03.setCode("03");
            commonDict03.setName("中共预备党员");
            commonDict03.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict03.setTypeName("政治面貌");
            politics.add(commonDict03);

            CommonDict commonDict04 = new CommonDict();
            commonDict04.setCode("04");
            commonDict04.setName("共青团员");
            commonDict04.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict04.setTypeName("政治面貌");
            politics.add(commonDict04);

            CommonDict commonDict05 = new CommonDict();
            commonDict05.setCode("05");
            commonDict05.setName("民革会员");
            commonDict05.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict05.setTypeName("政治面貌");
            politics.add(commonDict05);

            CommonDict commonDict06 = new CommonDict();
            commonDict06.setCode("06");
            commonDict06.setName("民盟盟员");
            commonDict06.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict06.setTypeName("政治面貌");
            politics.add(commonDict06);

            CommonDict commonDict07 = new CommonDict();
            commonDict07.setCode("07");
            commonDict07.setName("民建会员");
            commonDict07.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict07.setTypeName("政治面貌");
            politics.add(commonDict07);

            CommonDict commonDict08 = new CommonDict();
            commonDict08.setCode("08");
            commonDict08.setName("民进会员");
            commonDict08.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict08.setTypeName("政治面貌");
            politics.add(commonDict08);

            CommonDict commonDict09 = new CommonDict();
            commonDict09.setCode("09");
            commonDict09.setName("农工党党员");
            commonDict09.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict09.setTypeName("政治面貌");
            politics.add(commonDict09);

            CommonDict commonDict10 = new CommonDict();
            commonDict10.setCode("10");
            commonDict10.setName("致公党党员");
            commonDict10.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict10.setTypeName("政治面貌");
            politics.add(commonDict10);

            CommonDict commonDict11 = new CommonDict();
            commonDict11.setCode("11");
            commonDict11.setName("九三学社社员");
            commonDict11.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict11.setTypeName("政治面貌");
            politics.add(commonDict11);

            CommonDict commonDict12 = new CommonDict();
            commonDict12.setCode("12");
            commonDict12.setName("台盟盟员");
            commonDict12.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict12.setTypeName("政治面貌");
            politics.add(commonDict12);

            CommonDict commonDict13 = new CommonDict();
            commonDict13.setCode("13");
            commonDict13.setName("无党派民主人士");
            commonDict13.setType(CommonDictTypeEnum.POLITIC.getValue());
            commonDict13.setTypeName("政治面貌");
            politics.add(commonDict13);

            //保存
            commonDictRepository.saveAll(politics);
        }

        //添加受教育程度
        if (educations.size() == 0){
            CommonDict commonDict01 = new CommonDict();
            commonDict01.setCode("01");
            commonDict01.setName("小学及以下");
            commonDict01.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict01.setTypeName("文化程度");
            educations.add(commonDict01);

            CommonDict commonDict02 = new CommonDict();
            commonDict02.setCode("02");
            commonDict02.setName("初中");
            commonDict02.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict02.setTypeName("文化程度");
            educations.add(commonDict02);

            CommonDict commonDict03 = new CommonDict();
            commonDict03.setCode("03");
            commonDict03.setName("高中");
            commonDict03.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict03.setTypeName("文化程度");
            educations.add(commonDict03);

            CommonDict commonDict04 = new CommonDict();
            commonDict04.setCode("04");
            commonDict04.setName("高职/中专");
            commonDict04.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict04.setTypeName("文化程度");
            educations.add(commonDict04);

            CommonDict commonDict05 = new CommonDict();
            commonDict05.setCode("05");
            commonDict05.setName("大专");
            commonDict05.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict05.setTypeName("文化程度");
            educations.add(commonDict05);

            CommonDict commonDict06 = new CommonDict();
            commonDict06.setCode("06");
            commonDict06.setName("本科");
            commonDict06.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict06.setTypeName("文化程度");
            educations.add(commonDict06);

            CommonDict commonDict07 = new CommonDict();
            commonDict07.setCode("07");
            commonDict07.setName("研究生");
            commonDict07.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict07.setTypeName("文化程度");
            educations.add(commonDict07);

            CommonDict commonDict08 = new CommonDict();
            commonDict08.setCode("08");
            commonDict08.setName("硕士");
            commonDict08.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict08.setTypeName("文化程度");
            educations.add(commonDict08);

            CommonDict commonDict09 = new CommonDict();
            commonDict09.setCode("09");
            commonDict09.setName("博士及以上");
            commonDict09.setType(CommonDictTypeEnum.EDUCATION.getValue());
            commonDict09.setTypeName("文化程度");
            educations.add(commonDict09);

            //保存
            commonDictRepository.saveAll(educations);
        }
        //添加受教育程度
        if (jobs.size() == 0){
            CommonDict commonDict01 = new CommonDict();
            commonDict01.setCode("01");
            commonDict01.setName("代表");
            commonDict01.setType(CommonDictTypeEnum.JOBS.getValue());
            commonDict01.setTypeName("代表职务");
            jobs.add(commonDict01);

            CommonDict commonDict02 = new CommonDict();
            commonDict02.setCode("02");
            commonDict02.setName("人大领导");
            commonDict02.setType(CommonDictTypeEnum.JOBS.getValue());
            commonDict02.setTypeName("代表职务");
            jobs.add(commonDict02);

            CommonDict commonDict03 = new CommonDict();
            commonDict03.setCode("03");
            commonDict03.setName("办公室人员");
            commonDict03.setType(CommonDictTypeEnum.JOBS.getValue());
            commonDict03.setTypeName("代表职务");
            jobs.add(commonDict03);

            //保存
            commonDictRepository.saveAll(jobs);
        }
    }

    //初始化一个Account
    private void initOneAccount(String username, String mobile, String rawPwd) {
        Account account = accountRepository.findByUsername(username);//这里用户名查询就行了，用户名不可重复，管理员有可能换手机号
        if (account == null) {
            account = new Account();
            account.setUsername(username);
            account.setMobile(mobile);
            account.setLoginTimes(0);  //登录次数初始化为0
            account.setLoginWay((byte) 1);//账号密码方式登录
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.toString());
            Set<AccountRole> accountRoles = Sets.newHashSet();
            accountRoles.add(accountRole);
            account.setAccountRoles(accountRoles);
            accountRepository.saveAndFlush(account);
        }

        LoginUP loginUP = loginUPRepository.findByUsername(username);
        if (loginUP == null) {
            loginUP = new LoginUP();
            loginUP.setUsername(username);
            loginUP.setPassword(passwordEncoder.encode(rawPwd)); //保存hash后的密码
            loginUP.setAccount(account);
            loginUPRepository.saveAndFlush(loginUP);
        }
    }

    private void insertWeichatMenu() {
        JSONArray array = new JSONArray();
        List<WeChatMenu> menus = new ArrayList<>();
        // 登录公众号
        String uniqueKey = "weichat_user_auth";
        WeChatMenu menu = weChatMenuRepository.findByUniqueKey(uniqueKey);
        if (menu == null) {
            menu = new WeChatMenu();
            JSONObject obj = new JSONObject();
            menu.setUniqueKey(uniqueKey);

            String name = "登录公众号";
            menu.setName(name);
            obj.put("name", name);

            String type = "view";
            menu.setType(type);
            obj.put("type", type);

            String url = String.format(
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s#wechat_redirect",
                    CURRENT_APPID,
                    REDIRECT_URL,
                    "snsapi_userinfo",
                    SysUtil.uid()

            );
            menu.setUrl(url);
            obj.put("url", url);
            menus.add(menu);
            array.add(obj);
        }

        // 跳转到小程序
        uniqueKey = "weichat_jumpto_miniprogram";
        menu = weChatMenuRepository.findByUniqueKey(uniqueKey);
        if (menu == null) {
            menu = new WeChatMenu();
            JSONObject obj = new JSONObject();
            menu.setUniqueKey(uniqueKey);

            String name = "跳转到小程序";
            menu.setName(name);
            obj.put("name", name);

            String type = "miniprogram";
            menu.setType(type);
            obj.put("type", type);

            String appid = env.getProperty("miniapp.appid");
            menu.setAppid(appid);
            obj.put("appid", appid);

            String url = env.getProperty("service_app.pagepath");
            menu.setUrl(url);
            obj.put("url", url);

            String pagepath = env.getProperty("service_app.pagepath");
            menu.setPagepath(pagepath);
            obj.put("pagepath", pagepath);

            menus.add(menu);
            array.add(obj);
        }

        if (!array.isEmpty()) {
            String appid = env.getProperty("service_app.appid");
            WeChatAccessToken token = weChatAccessTokenRepository.findByAppid(appid);
            if (token == null) {
                token = getToken();
            }
            if (token != null) {
                if (!verifyToken(token)) token = getToken();
                pushMenus(token, array, menus);
            }

        }

    }

    private void pushMenus(WeChatAccessToken token, JSONArray array, List<WeChatMenu> menus) {
        JSONObject root = new JSONObject();
        root.put("button", array);
        String postMenuUrl = String.format("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s", token.getAccessToken());
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> reqEntity = new HttpEntity<>(root.toJSONString(), headers);
        ResponseEntity<JSONObject> respEntity = restTemplate.postForEntity(postMenuUrl, reqEntity, JSONObject.class);
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject body = respEntity.getBody();
            if (body == null) return;
            if (body.getIntValue("errcode") == 0) {
                weChatMenuRepository.saveAll(menus);
                LOGGER.info("create menu success.");
                return;
            }
        }
        LOGGER.warn("create menu failed.");
    }

//    @Scheduled(cron = "0 0/20 * * * ?")
//    private void refreshToken() {
//        getToken();
//    }

    public WeChatAccessToken getToken() {

        WeChatAccessToken token = weChatAccessTokenRepository.findByAppid(CURRENT_APPID);
        if (token != null) {
            if (verifyToken(token)) return token;
        } else {
            token = new WeChatAccessToken();
            token.setAppid(CURRENT_APPID);
        }

        LOGGER.info("get token at: {}", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        // 构造获取access_token 的url
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", CURRENT_APPID, CURRENT_APPSECRET);
        ResponseEntity<JSONObject> respEntity = restTemplate.getForEntity(url, JSONObject.class);
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject body = respEntity.getBody();
            if (body != null) {
                token.setAccessToken(body.getString("access_token"));
                weChatAccessTokenRepository.saveAndFlush(token);
                return token;
            }
        }

        return null;
    }

    public boolean verifyToken(WeChatAccessToken token) {
        if (token == null) return false;
        String url = String.format("https://api.weixin.qq.com/cgi-bin/menu/get?access_token=%s", token.getAccessToken());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> reqEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<JSONObject> respEntity = restTemplate.exchange(url, HttpMethod.GET, reqEntity, JSONObject.class);
        if (respEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject body = respEntity.getBody();
            if (body != null) {
                if (body.getIntValue("errcode") == 0) {
                    return true;
                }
            }
        }
        return false;
    }


    //为角色关联系统
    private void mapRoleAndSystems() {
        //选民
        AccountRole voter = accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword());
        Set<Systems> voterSystems = voter.getSystems();
        Set<Permission> voterPermissions = voter.getPermissions();

        voterSystems.add(systemRepository.findByName(SystemEnum.MEMBER_HOUSE.getName()));//代表之家
        voterSystems.add(systemRepository.findByName(SystemEnum.VOTE.getName()));//投票系统
        voter.setPermissions(voterPermissions);
        accountRoleRepository.save(voter);

        //后台管理员
        AccountRole bgAdmin = accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword());

        Set<Systems> bgAdminSystems = bgAdmin.getSystems();

        bgAdminSystems.add(systemRepository.findByName(SystemEnum.MEMBER_HOUSE.getName()));//代表之家
        bgAdminSystems.add(systemRepository.findByName(SystemEnum.BASIC_INFO.getName()));//基本信息管理
        bgAdminSystems.add(systemRepository.findByName(SystemEnum.SUGGESTION.getName()));//建议办理
        bgAdminSystems.add(systemRepository.findByName(SystemEnum.VOTE.getName()));//投票系统

        bgAdmin.setSystems(bgAdminSystems);
        accountRoleRepository.save(bgAdmin);

        //代表
        AccountRole member = accountRoleRepository.findByKeyword(AccountRoleEnum.NPC_MEMBER.getKeyword());
        Set<Systems> memberSystems  = member.getSystems();

        memberSystems.add(systemRepository.findByName(SystemEnum.MEMBER_HOUSE.getName()));//代表之家
        memberSystems.add(systemRepository.findByName(SystemEnum.SUGGESTION.getName()));//建议办理
        memberSystems.add(systemRepository.findByName(SystemEnum.VOTE.getName()));//投票系统

        member.setSystems(memberSystems);
        accountRoleRepository.save(member);


        //政府
        AccountRole  government = accountRoleRepository.findByKeyword(AccountRoleEnum.GOVERNMENT.getKeyword());
        Set<Systems> govermentSystems  = government.getSystems();

        govermentSystems.add(systemRepository.findByName(SystemEnum.SUGGESTION.getName()));//建议办理

        member.setSystems(govermentSystems);
        accountRoleRepository.save(government);

        //办理单位
        AccountRole  unit = accountRoleRepository.findByKeyword(AccountRoleEnum.UNIT.getKeyword());
        Set<Systems> unitSystems  = unit.getSystems();

        unitSystems.add(systemRepository.findByName(SystemEnum.SUGGESTION.getName()));//建议办理

        unit.setSystems(unitSystems);
        accountRoleRepository.save(unit);





    }
}
