package com.cdkhd.npc.config;

import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

//数据初始化配置类
@Configuration
public class DBInit {
    private AccountRoleRepository accountRoleRepository;
    private NpcMemberRoleRepository npcMemberRoleRepository;
    private PermissionRepository permissionRepository;
    private SystemRepository systemRepository;
    private MenuRepository menuRepository;
    private CommonDictRepository commonDictRepository;
    private Environment env;
    private AreaRepository areaRepository;
    private AccountRepository accountRepository;
    private LoginUPRepository loginUPRepository;
    private BackgroundAdminRepository backgroundAdminRepository;
    private SystemSettingRepository systemSettingRepository;
    private SessionRepository sessionRepository;
    private PerformanceTypeRepository performanceTypeRepository;

    @Autowired
    public DBInit(AccountRoleRepository accountRoleRepository, NpcMemberRoleRepository npcMemberRoleRepository, PermissionRepository permissionRepository, SystemRepository systemRepository, MenuRepository menuRepository, CommonDictRepository commonDictRepository, Environment env, AreaRepository areaRepository, AccountRepository accountRepository, LoginUPRepository loginUPRepository, BackgroundAdminRepository backgroundAdminRepository, SystemSettingRepository systemSettingRepository, SessionRepository sessionRepository, PerformanceTypeRepository performanceTypeRepository) {
        this.accountRoleRepository = accountRoleRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.permissionRepository = permissionRepository;
        this.systemRepository = systemRepository;
        this.menuRepository = menuRepository;
        this.commonDictRepository = commonDictRepository;
        this.env = env;
        this.areaRepository = areaRepository;
        this.accountRepository = accountRepository;
        this.loginUPRepository = loginUPRepository;
        this.backgroundAdminRepository = backgroundAdminRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.sessionRepository = sessionRepository;
        this.performanceTypeRepository = performanceTypeRepository;
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
//      mapMenuSystem();
        initArea();
        initAccount();
        initBackgroundAdmin();
        initSession();
        initSystemSetting();
        initPerformanceType();
    }

    private void initPerformanceType() {
        //初始化AccountRole
        String areaName = env.getProperty("npc_base_info.area");
        Area area = areaRepository.findByName(areaName);
        for (PerformanceTypeEnum performanceTypeEnum : PerformanceTypeEnum.values()) {
            PerformanceType performanceType = performanceTypeRepository.findByNameAndLevelAndAreaUidAndStatusAndIsDelFalse(performanceTypeEnum.getValue(),LevelEnum.AREA.getValue(),area.getUid(),StatusEnum.ENABLED.getValue());
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
        String username = env.getProperty("npc_base_info.user.name");
        String mobile = env.getProperty("npc_base_info.user.mobile");
        Account account = accountRepository.findByUsernameAndMobile(username,mobile);
        if (account == null){
            account = new Account();
            account.setUsername(username);
            account.setMobile(mobile);
            account.setLoginTimes(0);  //登录次数初始化为0
            account.setLoginWay((byte)1);//账号密码方式登录
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.toString());
            Set<AccountRole> accountRoles = Sets.newHashSet();
            accountRoles.add(accountRole);
            account.setAccountRoles(accountRoles);
            accountRepository.saveAndFlush(account);
        }

        LoginUP loginUP = loginUPRepository.findByUsername(username);
        if (loginUP == null){
            loginUP = new LoginUP();
            loginUP.setMobile(mobile);
            loginUP.setUsername(username);
            loginUP.setPassword("123456");
            loginUP.setAccount(account);
            loginUPRepository.saveAndFlush(loginUP);
        }

        username = env.getProperty("npc_base_info.cdkhd.name");
        mobile = env.getProperty("npc_base_info.cdkhd.mobile");
        account = accountRepository.findByUsernameAndMobile(username,mobile);
        if (account == null){
            account = new Account();
            account.setUsername(username);
            account.setMobile(mobile);
            account.setLoginTimes(0);  //登录次数初始化为0
            account.setLoginWay((byte)1);//账号密码方式登录
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.toString());
            Set<AccountRole> accountRoles = Sets.newHashSet();
            accountRoles.add(accountRole);
            account.setAccountRoles(accountRoles);
            accountRepository.saveAndFlush(account);
        }
        loginUP = loginUPRepository.findByUsername(username);
        if (loginUP == null){
            loginUP = new LoginUP();
            loginUP.setMobile(mobile);
            loginUP.setUsername(username);
            loginUP.setPassword("123456");
            loginUP.setAccount(account);
            loginUPRepository.saveAndFlush(loginUP);
        }
    }

    private void initBackgroundAdmin() {
        String areaName = env.getProperty("npc_base_info.area");
        Area area = areaRepository.findByName(areaName);
        String username = env.getProperty("npc_base_info.user.name");
        String mobile = env.getProperty("npc_base_info.user.mobile");
        Account account = accountRepository.findByUsernameAndMobile(username,mobile);
        BackgroundAdmin backgroundAdmin = backgroundAdminRepository.findByAccountUsername(username);
        if (backgroundAdmin == null) {
            backgroundAdmin = new BackgroundAdmin();
            backgroundAdmin.setAccount(account);
            backgroundAdmin.setLevel(LevelEnum.AREA.getValue());
            backgroundAdmin.setArea(area);
            backgroundAdminRepository.saveAndFlush(backgroundAdmin);
        }
        username = env.getProperty("npc_base_info.cdkhd.name");
        mobile = env.getProperty("npc_base_info.cdkhd.mobile");
        account = accountRepository.findByUsernameAndMobile(username,mobile);
        backgroundAdmin = backgroundAdminRepository.findByAccountUsername(username);
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
                systemRepository.save(systems);
            }
        }
    }

    //初始化菜单
    private void initMenu() {
        //遍历Menu枚举，初始化菜单
        for (MenuEnum menuEnum : MenuEnum.values()) {
            Menu menu = menuRepository.findByName(menuEnum.getName());
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
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));   //代表风采
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));   //我的意见
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.ANNOUNCEMENT.getKeyword()));   //接收公告
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));   //代表排名
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));   //各镇排名
        voter.setPermissions(voterPermissions);
        accountRoleRepository.save(voter);

        //后台管理员
        AccountRole bgAdmin = accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword());
        Set<Permission> bgAdminPermissions = bgAdmin.getPermissions();
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.HOMEPAGE.getKeyword()));//首页
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.ACCOUNT_MANAGE.getKeyword()));//账号管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NEWS_MANAGE.getKeyword()));//新闻管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NOTICE_MANAGE.getKeyword()));//通知管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NPC_MANAGE.getKeyword()));//代表管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GROUP_MANAGE.getKeyword()));//代表小组管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.WORKSTATION_MANAGE.getKeyword()));//工作站管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.OPINION_MANAGE.getKeyword()));//选民意见管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STUDY_TYPE_MANAGE.getKeyword()));//学习类型管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STUDY_MANAGE.getKeyword()));//学习资料管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_TYPE.getKeyword()));//代表建议管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_MANAGE.getKeyword()));//代表建议类型
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_COUNT.getKeyword()));//代表建议统计
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_TYPE.getKeyword()));//代表履职类型
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_MANAGE.getKeyword()));//代表履职管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_COUNT.getKeyword()));//代表履职统计
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_MANAGE.getKeyword()));//镇管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VILLAGE_MANAGE.getKeyword()));//村管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERMISSION_MANAGE.getKeyword()));//代表权限管理
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SESSION_MANAGE.getKeyword()));//届期管理
        bgAdmin.setPermissions(bgAdminPermissions);
        accountRoleRepository.save(bgAdmin);

        //普通代表
        NpcMemberRole member = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.MEMBER.getKeyword());
        Set<Permission> memberPermissions = member.getPermissions();
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));//代表风采
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));//我的意见
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));//收到意见
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));//我的建议
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));//我的履职
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));//接收通知
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.ANNOUNCEMENT.getKeyword()));//接收公告
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));//代表排名
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));//各镇排名
        member.setPermissions(memberPermissions);
        npcMemberRoleRepository.save(member);

        //人大主席（暂时与普通代表权限相同）
        NpcMemberRole chairman = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.CHAIRMAN.getKeyword());
        Set<Permission> chairmanPermissions = chairman.getPermissions();
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));//代表风采
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));//我的意见
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));//收到意见
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));//我的建议
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));//我的履职
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));//接收通知
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.ANNOUNCEMENT.getKeyword()));//接收公告
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));//代表排名
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));//各镇排名
        chairman.setPermissions(chairmanPermissions);
        npcMemberRoleRepository.save(chairman);

        //特殊人员
        NpcMemberRole specialMan = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.SPECIAL_MAN.getKeyword());
        Set<Permission> specialPermissions = specialMan.getPermissions();
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));//代表风采
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));   //查看联络点
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));//我的意见
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));//我的建议
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));//接收通知
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.ANNOUNCEMENT.getKeyword()));//接收公告
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));//代表排名
        specialPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));//各镇排名
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
        suggestionAuditorPermissions.add(permissionRepository.findByKeyword(PermissionEnum.AUDIT_SUGGESTION.getKeyword()));//建议审核
        suggestionAuditor.setPermissions(suggestionAuditorPermissions);
        npcMemberRoleRepository.save(suggestionAuditor);

        //通知公告审核人
        NpcMemberRole noticeAuditor = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword());
        Set<Permission> noticeAuditorPermissions = noticeAuditor.getPermissions();
        noticeAuditorPermissions.add(permissionRepository.findByKeyword(PermissionEnum.AUDIT_NOTICE.getKeyword()));//通知审核
        noticeAuditor.setPermissions(noticeAuditorPermissions);
        npcMemberRoleRepository.save(noticeAuditor);

    }

    //为权限关联菜单
    private void mapPermissionMenu() {

        //代表风采
        Menu menu = menuRepository.findByName(MenuEnum.MEMBER_INFO.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MEMBER_INFO.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //查看联络点
        menu = menuRepository.findByName(MenuEnum.WORK_STATION.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.VIEW_WORKSTATION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //我的意见
        menu = menuRepository.findByName(MenuEnum.MY_OPINION.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //我的建议
        menu = menuRepository.findByName(MenuEnum.MY_SUGGESTION.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //我的履职
        menu = menuRepository.findByName(MenuEnum.MY_PERFORMANCE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //收到的意见
        menu = menuRepository.findByName(MenuEnum.RECEIVE_OPINION.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //接收通知
        menu = menuRepository.findByName(MenuEnum.NOTIFICATION_INFO.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //接收公告
        menu = menuRepository.findByName(MenuEnum.ANNOUNCEMENT.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.ANNOUNCEMENT.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表排名
        menu = menuRepository.findByName(MenuEnum.MEMBER_RANK.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.MEMBER_RANK.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //各镇排名
        menu = menuRepository.findByName(MenuEnum.TOWN_RANK.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.TOWN_RANK.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核建议
        menu = menuRepository.findByName(MenuEnum.AUDIT_SUGGESTION.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_SUGGESTION.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核履职
        menu = menuRepository.findByName(MenuEnum.AUDIT_PERFORMANCE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_PERFORMANCE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核新闻
        menu = menuRepository.findByName(MenuEnum.AUDIT_NEWS.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_NEWS.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //审核通知
        menu = menuRepository.findByName(MenuEnum.AUDIT_NOTICE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.AUDIT_NOTICE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //****************后台菜单

        //首页
        menu = menuRepository.findByName(MenuEnum.HOMEPAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.HOMEPAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //账号管理
        menu = menuRepository.findByName(MenuEnum.ACCOUNT_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.ACCOUNT_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //新闻管理
        menu = menuRepository.findByName(MenuEnum.NEWS_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NEWS_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //通知管理
        menu = menuRepository.findByName(MenuEnum.NOTICE_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NOTICE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表管理
        menu = menuRepository.findByName(MenuEnum.NPC_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.NPC_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //小组管理
        menu = menuRepository.findByName(MenuEnum.NPC_MEMBER_GROUP.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.GROUP_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //联络点管理
        menu = menuRepository.findByName(MenuEnum.WORKSTATION_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.WORKSTATION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //选民意见管理
        menu = menuRepository.findByName(MenuEnum.OPINION_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.OPINION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //学习资料管理
        menu = menuRepository.findByName(MenuEnum.STUDY_TYPE_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.STUDY_TYPE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //学习资料管理
        menu = menuRepository.findByName(MenuEnum.STUDY_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.STUDY_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表建议类型
        menu = menuRepository.findByName(MenuEnum.SUGGESTION_TYPE_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_TYPE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表建议管理
        menu = menuRepository.findByName(MenuEnum.SUGGESTION_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表履职类型
        menu = menuRepository.findByName(MenuEnum.PERFORMANCE_TYPE_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_TYPE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表履职管理
        menu = menuRepository.findByName(MenuEnum.PERFORMANCE_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表建议统计
        menu = menuRepository.findByName(MenuEnum.SUGGESTION_COUNT.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_COUNT.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表履职统计
        menu = menuRepository.findByName(MenuEnum.PERFORMANCE_COUNT.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_COUNT.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //代表权限管理
        menu = menuRepository.findByName(MenuEnum.PERMISSION_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.PERMISSION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //镇管理
        menu = menuRepository.findByName(MenuEnum.TOWN_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.TOWN_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //村管理
        menu = menuRepository.findByName(MenuEnum.VILLAGE_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.VILLAGE_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //届期管理
        menu = menuRepository.findByName(MenuEnum.SESSION_MANAGE.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SESSION_MANAGE.getKeyword()));
        menuRepository.saveAndFlush(menu);

        //系统设置
        menu = menuRepository.findByName(MenuEnum.SYSTEM_SETTING.getName());
        menu.setPermission(permissionRepository.findByKeyword(PermissionEnum.SYSTEM_SETTING.getKeyword()));
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
            commonDict01.setName("普通代表");
            commonDict01.setType(CommonDictTypeEnum.JOBS.getValue());
            commonDict01.setTypeName("代表职务");
            jobs.add(commonDict01);

            CommonDict commonDict02 = new CommonDict();
            commonDict02.setCode("02");
            commonDict02.setName("人大主席");
            commonDict02.setType(CommonDictTypeEnum.JOBS.getValue());
            commonDict02.setTypeName("代表职务");
            jobs.add(commonDict02);

            CommonDict commonDict03 = new CommonDict();
            commonDict03.setCode("03");
            commonDict03.setName("特殊人员");
            commonDict03.setType(CommonDictTypeEnum.JOBS.getValue());
            commonDict03.setTypeName("代表职务");
            jobs.add(commonDict03);

            //保存
            commonDictRepository.saveAll(jobs);
        }
    }
}
