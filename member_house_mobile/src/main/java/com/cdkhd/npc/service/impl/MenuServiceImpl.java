package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.MenuVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.member_house.*;
import com.cdkhd.npc.service.MenuService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuServiceImpl implements MenuService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuServiceImpl.class);

    private AccountRepository accountRepository;

    private AccountRoleRepository accountRoleRepository;

    private NpcMemberRepository npcMemberRepository;

    private NpcMemberRoleRepository npcMemberRoleRepository;

    private MenuRepository menuRepository;

    private OpinionRepository opinionRepository;

    private OpinionReplayRepository opinionReplayRepository;

    private SuggestionRepository suggestionRepository;

    private SuggestionReplyRepository suggestionReplyRepository;

    private PerformanceRepository performanceRepository;

    private NewsRepository newsRepository;

    private SystemSettingRepository systemSettingRepository;

    private SystemSettingService systemSettingService;



    @Autowired
    public MenuServiceImpl(AccountRepository accountRepository, AccountRoleRepository accountRoleRepository, NpcMemberRepository npcMemberRepository, NpcMemberRoleRepository npcMemberRoleRepository, MenuRepository menuRepository, OpinionRepository opinionRepository, OpinionReplayRepository opinionReplayRepository, SuggestionRepository suggestionRepository, SuggestionReplyRepository suggestionReplyRepository, PerformanceRepository performanceRepository, NewsRepository newsRepository, SystemSettingRepository systemSettingRepository, SystemSettingService systemSettingService) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.menuRepository = menuRepository;
        this.opinionRepository = opinionRepository;
        this.opinionReplayRepository = opinionReplayRepository;
        this.suggestionRepository = suggestionRepository;
        this.suggestionReplyRepository = suggestionReplyRepository;
        this.performanceRepository = performanceRepository;
        this.newsRepository = newsRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.systemSettingService = systemSettingService;
    }


    @Override
    public RespBody getMenus(UserDetailsImpl userDetails, String system, Byte level) {
        RespBody body = new RespBody();
        if (userDetails == null) {
            body.setMessage("用户未登录");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        if (account == null) {
            body.setMessage("用户未登录");
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
        if (StringUtils.isEmpty(system)){
            body.setMessage("请选择系统");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        //代表的具体职能身份
        Set<NpcMemberRole> roles = Sets.newHashSet();
        NpcMember npcMember = new NpcMember();
        //返回的json数据
        JSONObject object = new JSONObject();
        if (CollectionUtils.isEmpty(account.getNpcMembers())) {
            //非代表可能是政府人员或者是办理单位人员
            List<AccountRole> accountRoles = Lists.newArrayList(account.getAccountRoles());
            if (accountRoles.size()>1) {
                for (AccountRole accountRole : accountRoles) {
                    if (!accountRole.getKeyword().equals(AccountRoleEnum.VOTER.getKeyword())){
                        object.put("ROLE",accountRole.getKeyword());
                        object.put("ROLE_NAME", accountRole.getName());
                    }
                }
            }else{
                object.put("ROLE",accountRoles.get(0).getKeyword());
                object.put("ROLE_NAME", accountRoles.get(0).getName());
            }
        } else {
            //这里不能切换为选民身份、是代表就只能是代表
            npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
            if (npcMember == null){
                object.put("ROLE",AccountRoleEnum.VOTER.getKeyword());
                object.put("ROLE_NAME", AccountRoleEnum.VOTER.getName());
            }else {
                roles = npcMember.getNpcMemberRoles();
                for (NpcMemberRole role : roles) {//展示必要的身份
                    if (role.getIsMust()){
                        object.put("ROLE",role.getKeyword());
                        object.put("ROLE_NAME", role.getName());
                        break;
                    }
                }
            }
        }

        boolean hasMobile = StringUtils.isNotBlank(account.getMobile());//用户是否注册成功
        if (!hasMobile){
            body.setMessage("请先注册");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        SystemSetting systemSetting = systemSettingService.getSystemSetting(userDetails);
        List<Menu> menus = Lists.newArrayList();//当前用户应该展示的菜单
        Set<String> menuUid = new HashSet<>();
        List<Menu> systemMenus = menuRepository.findBySystemsUidAndEnabled(system, StatusEnum.ENABLED.getValue());//当前系统下的所有菜单
        if (CollectionUtils.isNotEmpty(roles)) {
            for (NpcMemberRole role : roles) {
                if (!role.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;
                if (!systemSetting.getPerformanceGroupAudit() && (role.getKeyword().equals(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword()) && level.equals(npcMember.getLevel()))) continue;
                Set<Permission> permissions = role.getPermissions();
                if (permissions != null && !permissions.isEmpty()) {
                    for (Permission permission : permissions) {
                        if (!permission.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;
                        Set<Menu> miniAppMenus = permission.getMenus();
                        if (miniAppMenus != null && !miniAppMenus.isEmpty()) {
                            miniAppMenus.retainAll(systemMenus);//当前权限下的菜单和当前系统下的菜单取交集
                            for (Menu menu : miniAppMenus) {
                                if (!menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) continue;
                                // 用户已经绑定了手机就不再需要提供该功能
                                if (!menuUid.add(menu.getUid())) continue;
                                menus.add(menu);
                            }
                        }
                    }
                }
            }
        }
        menus.sort(Comparator.comparing(Menu::getId));//按id排序
        List<MenuVo> menuVos = this.dealChildren(menus);
        object.put("MENUS", menuVos);
        body.setData(object);
        return body;
    }

    /**
     * 将子菜单放到对应的模块下
     * @param menus
     * @return
     */
    private List<MenuVo> dealChildren(List<Menu> menus) {
        List<MenuVo> menuVos = Lists.newArrayList();
        for (Menu menu : menus) {
            if (StringUtils.isEmpty(menu.getParentId())){//先把一级菜单装下
                menuVos.add(MenuVo.convert(menu));
            }
        }
        for (Menu menu : menus) {
            if (StringUtils.isNotEmpty(menu.getParentId())){//再把二级菜单装在一级菜单下
                for (MenuVo menuVo : menuVos) {
                    if (menuVo.getUid().equals(menu.getParentId())){
                        List<MenuVo> children = menuVo.getChildren();
                        children.add(MenuVo.convert(menu));
                        menuVo.setChildren(children);
                    }
                }
            }
        }
        return menuVos;
    }

    @Override
    public RespBody countUnRead(UserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
        JSONObject obj = new JSONObject();
        //我收到的的意见回复数量
        List<OpinionReply> opinionReplies = opinionReplayRepository.findAll((Specification<OpinionReply>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(cb.equal(root.get("opinion").get("level").as(Byte.class),level));
            predicateList.add(cb.equal(root.get("opinion").get("area").get("uid").as(String.class),userDetails.getArea().getUid()));
            if (level.equals(LevelEnum.TOWN.getValue())){
                predicateList.add(cb.equal(root.get("opinion").get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
            }
            predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
            predicateList.add(cb.equal(root.get("opinion").get("sender").get("uid").as(String.class), account.getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        obj.put("OPINION_REPLY", opinionReplies.size());

        //我收到的意见数量
        List<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(cb.equal(root.get("level").as(Byte.class),level));
            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class),userDetails.getArea().getUid()));
            if (level.equals(LevelEnum.TOWN.getValue())){
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
            }
            predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
            predicateList.add(cb.equal(root.get("sender").get("uid").as(String.class), account.getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        obj.put("OPINION", opinions.size());

        //我收到的建议回复数量
        List<SuggestionReply> suggestionReplies = suggestionReplyRepository.findAll((Specification<SuggestionReply>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(cb.equal(root.get("suggestion").get("level").as(Byte.class),level));
            predicateList.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class),userDetails.getArea().getUid()));
            if (level.equals(LevelEnum.TOWN.getValue())){
                predicateList.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
            }
            predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
            predicateList.add(cb.equal(root.get("suggestion").get("sender").get("uid").as(String.class), npcMember.getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        obj.put("SUGGESTION_REPLY", suggestionReplies.size());

        //我提出的履职审核数量
        List<Performance> performances = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(cb.equal(root.get("level").as(Byte.class),level));
            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class),userDetails.getArea().getUid()));
            predicateList.add(cb.notEqual(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            predicateList.add(cb.equal(root.get("myView").as(Boolean.class), false));
            predicateList.add(cb.equal(root.get("isDel").as(Boolean.class), false));
            if (level.equals(LevelEnum.TOWN.getValue())){
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
            }
            predicateList.add(cb.equal(root.get("member").get("uid").as(String.class), npcMember.getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        obj.put("PERFORMANCES", performances.size());

        //todo 还没提交代码我收到的通知数量
        List<NotificationDetail> notificationDetails = Lists.newArrayList();
        obj.put("NOTIFICATION_DETAILS", notificationDetails.size());

        //我收到的建议条数
        List<Suggestion> suggestions = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(cb.equal(root.get("level").as(Byte.class),level));
            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class),userDetails.getArea().getUid()));
            if (level.equals(LevelEnum.TOWN.getValue())){
                predicateList.add(cb.equal(root.get("uid").as(String.class),userDetails.getTown().getUid()));
            }
            predicateList.add(cb.equal(root.get("status").as(Byte.class), (byte)3));//todo 建议状态
            predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
            predicateList.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        obj.put("SUGGESTIONS", suggestions.size());

        //需要审核的新闻条数
        List<News> news = newsRepository.findAll((Specification<News>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(cb.equal(root.get("level").as(Byte.class),level));
            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class),userDetails.getArea().getUid()));
            if (level.equals(LevelEnum.TOWN.getValue())){
                predicateList.add(cb.equal(root.get("uid").as(String.class),userDetails.getTown().getUid()));
            }
            predicateList.add(cb.equal(root.get("status").as(Byte.class), NewsStatusEnum.UNDER_REVIEW.ordinal()));//todo 新闻状态为待审核
            predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        obj.put("NEWS", news.size());

        //todo 代码还未提交 待审核的通知
        List<Notification> notifications = Lists.newArrayList();
        obj.put("NOTIFICATIONS", notifications.size());

        //履职总审核人待审核的履职条数

        //小组审核人uid
        List<String> generalAuditorUids;
        List<Performance> performanceGeneralList = Lists.newArrayList();
        SystemSetting systemSetting = systemSettingService.getSystemSetting(userDetails);
        if (systemSetting.getPerformanceGroupAudit()){//开启了开关，只审核小组审核人的履职
            NpcMemberRole generalAuditorRole = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());
            generalAuditorUids = generalAuditorRole.getNpcMembers().stream().filter(member -> member.getLevel().equals(npcMember.getLevel()) && ((member.getLevel().equals(LevelEnum.AREA.getValue()) && member.getArea().getUid().equals(npcMember.getArea().getUid())) || (member.getLevel().equals(LevelEnum.TOWN.getValue())) && member.getTown().getUid().equals(npcMember.getTown().getUid()))).map(NpcMember::getUid).collect(Collectors.toList());
        }else{//关闭了开关审核所有人的履职
            if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                generalAuditorUids = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(npcMember.getTown().getUid(),npcMember.getLevel()).stream().map(NpcMember::getUid).collect(Collectors.toList());
            }else{
                generalAuditorUids = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(npcMember.getArea().getUid(),npcMember.getLevel()).stream().map(NpcMember::getUid).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isNotEmpty(generalAuditorUids)) {
            performanceGeneralList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicateList.add(cb.notEqual(root.get("status").as(int.class), StatusEnum.ENABLED.getValue()));
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                predicateList.add(cb.equal(root.get("isDel").as(Boolean.class), false));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                in.value(generalAuditorUids);
                predicateList.add(in);
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
        }
        obj.put("PERFORMANCE_TO_GENERAL_AUDITOR", performanceGeneralList.size());

        //小组审核人待审核数量
        NpcMemberRole auditorRole = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());
        List<String> auditorUids = auditorRole.getNpcMembers().stream().filter(member -> member.getLevel().equals(npcMember.getLevel()) && ((member.getLevel().equals(LevelEnum.AREA.getValue()) && member.getArea().getUid().equals(npcMember.getArea().getUid())) || (member.getLevel().equals(LevelEnum.TOWN.getValue())) && member.getTown().getUid().equals(npcMember.getTown().getUid()))).map(NpcMember::getUid).collect(Collectors.toList());
        List<Performance> performanceList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(auditorUids)) {
            performanceList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicateList.add(cb.notEqual(root.get("status").as(int.class), StatusEnum.ENABLED.getValue()));
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                predicateList.add(cb.equal(root.get("isDel").as(Boolean.class), false));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                in.value(auditorUids);
                predicateList.add(in);
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
        }
        obj.put("PERFORMANCE_TO_AUDITOR", performanceList.size());

        //代表
        body.setData(obj);
        return body;
    }
}
