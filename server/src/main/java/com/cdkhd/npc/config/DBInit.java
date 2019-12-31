package com.cdkhd.npc.config;

import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Set;

//数据初始化配置类
@Configuration
public class DBInit {
    private AccountRoleRepository accountRoleRepository;
    private NpcMemberRoleRepository npcMemberRoleRepository;
    private PermissionRepository permissionRepository;
    private SystemRepository systemRepository;
    private MenuRepository menuRepository;

    @Autowired
    public DBInit(AccountRoleRepository accountRoleRepository, NpcMemberRoleRepository npcMemberRoleRepository, PermissionRepository permissionRepository, SystemRepository systemRepository, MenuRepository menuRepository) {
        this.accountRoleRepository = accountRoleRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.permissionRepository = permissionRepository;
        this.systemRepository = systemRepository;
        this.menuRepository = menuRepository;
    }

    @PostConstruct
    public void init() {
        initRole();
        initPermission();
        initSystem();
        initMenu();

        mapRoleAndPermission();
        mapPermissionMenu();
        mapMenuSystem();
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
        Systems systems = systemRepository.findByName("代表之家系统");
        if (systems == null) {
            systems = new Systems();
            systems.setName("代表之家系统");
            systems.setEnabled(StatusEnum.ENABLED.getValue());
            systemRepository.save(systems);
        }

        systems = systemRepository.findByName("代表建议办理系统");
        if (systems == null) {
            systems = new Systems();
            systems.setName("代表建议办理系统");
            systems.setEnabled(StatusEnum.ENABLED.getValue());
            systemRepository.save(systems);
        }

        systems = systemRepository.findByName("代表履职登记系统");
        if (systems == null) {
            systems = new Systems();
            systems.setName("代表履职登记系统");
            systems.setEnabled(StatusEnum.ENABLED.getValue());
            systemRepository.saveAndFlush(systems);
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
                menuRepository.saveAndFlush(menu);
            }
        }
    }

    //为角色关联权限
    private void mapRoleAndPermission() {
        //选民
        AccountRole voter = accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword());
        Set<Permission> voterPermissions = voter.getPermissions();
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.EDIT_OPINION.getKeyword()));   //提意见
        voterPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword()));   //我的意见
        voter.setPermissions(voterPermissions);
        accountRoleRepository.save(voter);

        //后台管理员
        AccountRole bgAdmin = accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword());
        Set<Permission> bgAdminPermissions = bgAdmin.getPermissions();
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.ACCOUNT_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STUDY_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.WORKSTATION_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NEWS_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NOTICE_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.NPC_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.TOWN_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.GROUP_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.VILLAGE_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.OPINION_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.STATISTICS.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.PERMISSION_MANAGE.getKeyword()));
        bgAdminPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SESSION_MANAGE.getKeyword()));
        bgAdmin.setPermissions(bgAdminPermissions);
        accountRoleRepository.save(bgAdmin);

        //普通代表
        NpcMemberRole member = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.MEMBER.getKeyword());
        Set<Permission> memberPermissions = member.getPermissions();
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.EDIT_SUGGESTION.getKeyword()));
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.OTHERS_SUGGESTION.getKeyword()));
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SECONDED_SUGGESTION.getKeyword()));
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.EDIT_PERFORMANCE.getKeyword()));
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));
        memberPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));
        member.setPermissions(memberPermissions);
        npcMemberRoleRepository.save(member);

        //人大主席（暂时与普通代表权限相同）
        NpcMemberRole chairman = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.CHAIRMAN.getKeyword());
        Set<Permission> chairmanPermissions = chairman.getPermissions();
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword()));
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.EDIT_SUGGESTION.getKeyword()));
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword()));
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.OTHERS_SUGGESTION.getKeyword()));
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.SECONDED_SUGGESTION.getKeyword()));
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.EDIT_PERFORMANCE.getKeyword()));
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword()));
        chairmanPermissions.add(permissionRepository.findByKeyword(PermissionEnum.RECEIVE_NOTICE.getKeyword()));
        chairman.setPermissions(chairmanPermissions);
        npcMemberRoleRepository.save(chairman);
    }

    //为权限关联菜单
    private void mapPermissionMenu() {
        //我的意见
        Permission permission = permissionRepository.findByKeyword(PermissionEnum.MY_OPINION.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.MY_OPINION.getName()));
        permissionRepository.save(permission);

        //收到的意见
        permission = permissionRepository.findByKeyword(PermissionEnum.RECEIVE_OPINION.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.RECEIVE_OPINION.getName()));
        permissionRepository.save(permission);

        //提出建议
        permission = permissionRepository.findByKeyword(PermissionEnum.EDIT_SUGGESTION.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.EDIT_SUGGESTION.getName()));
        permissionRepository.save(permission);

        //我的建议
        permission = permissionRepository.findByKeyword(PermissionEnum.MY_SUGGESTION.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.MY_SUGGESTION.getName()));
        permissionRepository.save(permission);

        //审核建议
        permission = permissionRepository.findByKeyword(PermissionEnum.AUDIT_SUGGESTION.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.AUDIT_SUGGESTION.getName()));
        permissionRepository.save(permission);

        //我的附议建议
        permission = permissionRepository.findByKeyword(PermissionEnum.SECONDED_SUGGESTION.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.SECONDED_SUGGESTION.getName()));
        permissionRepository.save(permission);

        //添加履职
        permission = permissionRepository.findByKeyword(PermissionEnum.EDIT_PERFORMANCE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.EDIT_PERFORMANCE.getName()));
        permissionRepository.save(permission);

        //我的履职
        permission = permissionRepository.findByKeyword(PermissionEnum.MY_PERFORMANCE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.MY_PERFORMANCE.getName()));
        permissionRepository.save(permission);

        //审核履职
        permission = permissionRepository.findByKeyword(PermissionEnum.AUDIT_PERFORMANCE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.AUDIT_PERFORMANCE.getName()));
        permissionRepository.save(permission);

        //审核新闻
        permission = permissionRepository.findByKeyword(PermissionEnum.AUDIT_NEWS.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.AUDIT_NEWS.getName()));
        permissionRepository.save(permission);

        //审核通知
        permission = permissionRepository.findByKeyword(PermissionEnum.AUDIT_NOTICE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.AUDIT_NOTICE.getName()));
        permissionRepository.save(permission);

        //账号管理
        permission = permissionRepository.findByKeyword(PermissionEnum.ACCOUNT_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.ACCOUNT_MANAGE.getName()));
        permissionRepository.save(permission);

        //学习资料管理
        permission = permissionRepository.findByKeyword(PermissionEnum.STUDY_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.STUDY_MANAGE.getName()));
        permissionRepository.save(permission);

        //联络点管理
        permission = permissionRepository.findByKeyword(PermissionEnum.WORKSTATION_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.WORKSTATION_MANAGE.getName()));
        permissionRepository.save(permission);

        //新闻管理
        permission = permissionRepository.findByKeyword(PermissionEnum.NEWS_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.NEWS_MANAGE.getName()));
        permissionRepository.save(permission);

        //通知管理
        permission = permissionRepository.findByKeyword(PermissionEnum.NOTICE_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.NOTICE_MANAGE.getName()));
        permissionRepository.save(permission);

        //代表管理
        permission = permissionRepository.findByKeyword(PermissionEnum.NPC_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.NPC_MANAGE.getName()));
        permissionRepository.save(permission);

        //镇管理
        permission = permissionRepository.findByKeyword(PermissionEnum.TOWN_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.TOWN_MANAGE.getName()));
        permissionRepository.save(permission);

        //小组管理
        permission = permissionRepository.findByKeyword(PermissionEnum.GROUP_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.GROUP_MANAGE.getName()));
        permissionRepository.save(permission);

        //村管理
        permission = permissionRepository.findByKeyword(PermissionEnum.VILLAGE_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.VILLAGE_MANAGE.getName()));
        permissionRepository.save(permission);

        //选民意见管理
        permission = permissionRepository.findByKeyword(PermissionEnum.OPINION_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.OPINION_MANAGE.getName()));
        permissionRepository.save(permission);

        //代表建议管理
        permission = permissionRepository.findByKeyword(PermissionEnum.SUGGESTION_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.SUGGESTION_MANAGE.getName()));
        permissionRepository.save(permission);

        //代表履职管理
        permission = permissionRepository.findByKeyword(PermissionEnum.PERFORMANCE_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.PERFORMANCE_MANAGE.getName()));
        permissionRepository.save(permission);

        //统计分析
        permission = permissionRepository.findByKeyword(PermissionEnum.STATISTICS.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.STATISTICS.getName()));
        permissionRepository.save(permission);

        //代表权限管理
        permission = permissionRepository.findByKeyword(PermissionEnum.PERMISSION_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.PERMISSION_MANAGE.getName()));
        permissionRepository.save(permission);

        //届期管理
        permission = permissionRepository.findByKeyword(PermissionEnum.SESSION_MANAGE.getKeyword());
        permission.setMenu(menuRepository.findByName(MenuEnum.SESSION_MANAGE.getName()));
        permissionRepository.save(permission);
    }

    //为菜单关联系统
    private void mapMenuSystem() {
        for (Menu menu : menuRepository.findAll()) {
            menu.setSystems(systemRepository.findByName("代表之家系统"));
            menuRepository.save(menu);
        }

        Menu mySecondedSug = menuRepository.findByName(MenuEnum.SECONDED_SUGGESTION.getName());
        mySecondedSug.setSystems(systemRepository.findByName("代表建议办理系统"));
        menuRepository.save(mySecondedSug);
    }

}
