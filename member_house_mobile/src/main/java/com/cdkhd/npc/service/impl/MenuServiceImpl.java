package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.LevelVo;
import com.cdkhd.npc.entity.vo.MenuVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.member_house.*;
import com.cdkhd.npc.service.MenuService;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private NotificationRepository notificationRepository;

    private NotificationViewDetailRepository notificationViewDetailRepository;

    @Autowired
    public MenuServiceImpl(AccountRepository accountRepository, NpcMemberRepository npcMemberRepository, NpcMemberRoleRepository npcMemberRoleRepository, MenuRepository menuRepository, OpinionRepository opinionRepository, OpinionReplayRepository opinionReplayRepository, SuggestionRepository suggestionRepository, SuggestionReplyRepository suggestionReplyRepository, PerformanceRepository performanceRepository, NewsRepository newsRepository, SystemSettingRepository systemSettingRepository, NotificationRepository notificationRepository, NotificationViewDetailRepository notificationViewDetailRepository) {
        this.accountRepository = accountRepository;
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
        this.notificationRepository = notificationRepository;
        this.notificationViewDetailRepository = notificationViewDetailRepository;
    }

    @Override
    public RespBody getMenus(MobileUserDetailsImpl userDetails, String system, Byte level) {
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
        if (StringUtils.isEmpty(system)) {
            body.setMessage("请选择系统");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        //代表的具体职能身份
        Set<NpcMemberRole> roles = Sets.newHashSet();
        List<AccountRole> accountRoles = Lists.newArrayList();

        NpcMember npcMember = new NpcMember();
        //返回的json数据
        JSONObject object = new JSONObject();
        if (CollectionUtils.isEmpty(account.getNpcMembers())) {
            //非代表可能是政府人员或者是办理单位人员
            accountRoles = Lists.newArrayList(account.getAccountRoles());
            if (accountRoles.size() > 1) {
                for (AccountRole accountRole : accountRoles) {
                    if (!accountRole.getKeyword().equals(AccountRoleEnum.VOTER.getKeyword())) {
                        object.put("ROLE", accountRole.getKeyword());
                        object.put("ROLE_NAME", accountRole.getName());
                    }
                }
            } else {
                object.put("ROLE", accountRoles.get(0).getKeyword());
                object.put("ROLE_NAME", accountRoles.get(0).getName());
            }
        } else {
            //这里不能切换为选民身份、是代表就只能是代表
            npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
            if (npcMember == null) {
                object.put("ROLE", AccountRoleEnum.VOTER.getKeyword());
                object.put("ROLE_NAME", AccountRoleEnum.VOTER.getName());
            } else {
                roles = npcMember.getNpcMemberRoles();
                for (NpcMemberRole role : roles) {//展示必要的身份
                    if (role.getIsMust()) {
                        object.put("ROLE", role.getKeyword());
                        object.put("ROLE_NAME", role.getName());
                        break;
                    }
                }
            }
        }

        boolean hasMobile = StringUtils.isNotBlank(account.getMobile());//用户是否注册成功
        if (!hasMobile) {
            body.setMessage("请先注册");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        SystemSetting systemSetting = this.getSystemSetting(userDetails, level);
        List<Menu> menus = Lists.newArrayList();//当前用户应该展示的菜单
        List<Menu> systemMenus = menuRepository.findBySystemsUidAndEnabled(system, StatusEnum.ENABLED.getValue());//当前系统下的所有菜单
        if (CollectionUtils.isNotEmpty(roles)) {
            for (NpcMemberRole role : roles) {//代表拥有的角色
                if (!role.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//确保角色状态有效
                if (!systemSetting.getPerformanceGroupAudit() && (role.getKeyword().equals(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword()) && level.equals(npcMember.getLevel())))
                    continue;//开关关闭的时候，过滤掉小组审核人的审核菜单
                Set<Permission> permissions = role.getPermissions();
                if (permissions != null && !permissions.isEmpty()) {
                    for (Permission permission : permissions) {
                        if (!permission.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//确保权限状态有效
                        Set<Menu> miniAppMenus = permission.getMenus();//获取权限下的菜单
                        if (miniAppMenus != null && !miniAppMenus.isEmpty()) {
                            miniAppMenus.retainAll(systemMenus);//当前权限下的菜单和当前系统下的菜单取交集
                            for (Menu menu : miniAppMenus) {
                                if (!menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) continue;//菜单可用才展示
                                if (menu.getType().equals(StatusEnum.DISABLED.getValue())) continue;//如果是后台菜单，就过滤掉
                                menus.add(menu);
                            }
                        }
                    }
                }
            }
        }else if (CollectionUtils.isNotEmpty(accountRoles)){
            for (AccountRole role : accountRoles) {//代表拥有的角色
                if (!role.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//确保角色状态有效
                Set<Permission> permissions = role.getPermissions();
                if (permissions != null && !permissions.isEmpty()) {
                    for (Permission permission : permissions) {
                        if (!permission.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//确保权限状态有效
                        Set<Menu> miniAppMenus = permission.getMenus();//获取权限下的菜单
                        if (miniAppMenus != null && !miniAppMenus.isEmpty()) {
                            miniAppMenus.retainAll(systemMenus);//当前权限下的菜单和当前系统下的菜单取交集
                            for (Menu menu : miniAppMenus) {
                                if (!menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) continue;//菜单可用才展示
                                if (menu.getType().equals(StatusEnum.DISABLED.getValue())) continue;//如果是后台菜单，就过滤掉
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

    private SystemSetting getSystemSetting(MobileUserDetailsImpl userDetails, Byte level) {
        SystemSetting systemSetting = new SystemSetting();
        if (level.equals(LevelEnum.TOWN.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndTownUid(level, userDetails.getTown().getUid());
        } else if (level.equals(LevelEnum.AREA.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(level, userDetails.getArea().getUid());
        }
        return systemSetting;
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
            }
        }
        return menuVos;
    }

    @Override
    public RespBody countUnRead(MobileUserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
        JSONObject obj = new JSONObject();
        //我收到的的意见回复数量
        List<OpinionReply> opinionReplies = opinionReplayRepository.findAll((Specification<OpinionReply>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
//            predicateList.add(cb.equal(root.get("opinion").get("level").as(Byte.class), level));
            predicateList.add(cb.equal(root.get("opinion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (level.equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("opinion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
            predicateList.add(cb.equal(root.get("opinion").get("sender").get("uid").as(String.class), account.getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        obj.put(MenuEnum.MY_OPINION.toString(), opinionReplies.size());

        // 我收到的公告的数量
//        List<NotificationViewDetail> notifications = notificationViewDetailRepository.findAll((Specification<NotificationViewDetail>) (root, query, cb) -> {
//            List<Predicate> predicateList = new ArrayList<>();
//            predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
//            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
//            if (level.equals(LevelEnum.TOWN.getValue())) {
//                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
//            }
//            predicateList.add(cb.equal(root.get("isRead").as(Boolean.class), false));
//            predicateList.add(cb.equal(root.get("sender").get("uid").as(String.class), account.getUid()));
//            return cb.and(predicateList.toArray(new Predicate[0]));
//        });
//        obj.put(MenuEnum.NOTIFICATION.toString(), notifications.size());


        if(npcMember != null) {
            //我收到的意见数量
            List<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
//                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                predicateList.add(cb.equal(root.get("receiver").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.RECEIVE_OPINION.toString(), opinions.size());

            //我收到的建议回复数量
            List<SuggestionReply> suggestionReplies = suggestionReplyRepository.findAll((Specification<SuggestionReply>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                predicateList.add(cb.equal(root.get("suggestion").get("raiser").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.MY_SUGGESTION.toString(), suggestionReplies.size());

            //我提出的履职审核数量
            List<Performance> performances = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                predicateList.add(cb.isNotNull(root.get("status")));
                predicateList.add(cb.equal(root.get("myView").as(Boolean.class), false));
                predicateList.add(cb.equal(root.get("isDel").as(Boolean.class), false));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.MY_PERFORMANCE.toString(), performances.size());

            // 我收到的通知数量
            List<NotificationViewDetail> notificationViewDetails = notificationViewDetailRepository.findAll((Specification<NotificationViewDetail>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("notification").get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("notification").get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("notification").get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.isFalse(root.get("isRead").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("receiver").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.NOTIFICATION_INFO.toString(), notificationViewDetails.size());

            //我收到的建议条数
            List<Suggestion> suggestions = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("uid").as(String.class), userDetails.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));//todo 建议状态
                predicateList.add(cb.isFalse(root.get("view").as(Boolean.class)));
//                predicateList.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.AUDIT_SUGGESTION.toString(), suggestions.size());

            //需要审核的新闻条数
            List<News> news = newsRepository.findAll((Specification<News>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("status").as(Integer.class), NewsStatusEnum.UNDER_REVIEW.ordinal()));//todo 新闻状态为待审核
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.AUDIT_NEWS.toString(), news.size());

            // 待审核的通知
            List<Notification> notificationAuditors = notificationRepository.findAll((Specification<Notification>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("status").as(Integer.class), NotificationStatusEnum.UNDER_REVIEW.ordinal()));//todo 新闻状态为待审核
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.AUDIT_NOTICE.toString(), notificationAuditors.size());

            //履职总审核人待审核的履职条数

            if (npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList()).contains(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword())) {
                //小组审核人uid
                List<String> generalAuditorUids;
                List<Performance> performanceGeneralList = Lists.newArrayList();
                SystemSetting systemSetting = this.getSystemSetting(userDetails, level);
                if (systemSetting.getPerformanceGroupAudit()) {//开启了开关，只审核小组审核人的履职
                    NpcMemberRole generalAuditorRole = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());
                    generalAuditorUids = generalAuditorRole.getNpcMembers().stream().filter(member -> member.getLevel().equals(npcMember.getLevel()) && ((member.getLevel().equals(LevelEnum.AREA.getValue()) && member.getArea().getUid().equals(npcMember.getArea().getUid())) || (member.getLevel().equals(LevelEnum.TOWN.getValue())) && member.getTown().getUid().equals(npcMember.getTown().getUid()))).map(NpcMember::getUid).collect(Collectors.toList());
                } else {//关闭了开关审核所有人的履职
                    if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                        generalAuditorUids = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(npcMember.getTown().getUid(), npcMember.getLevel()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    } else {
                        generalAuditorUids = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(npcMember.getArea().getUid(), npcMember.getLevel()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    }
                }
                if (CollectionUtils.isNotEmpty(generalAuditorUids)) {
                    performanceGeneralList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
                        List<Predicate> predicateList = new ArrayList<>();
                        predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                        predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                        predicateList.add(cb.isNull(root.get("status")));
                        predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                        predicateList.add(cb.equal(root.get("isDel").as(Boolean.class), false));
                        if (level.equals(LevelEnum.TOWN.getValue())) {
                            predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                        }
                        CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                        in.value(generalAuditorUids);
                        predicateList.add(in);
                        return cb.and(predicateList.toArray(new Predicate[0]));
                    });
                }
                obj.put(MenuEnum.AUDIT_PERFORMANCE.toString(), performanceGeneralList.size());
            }
            if (npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList()).contains(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword())) {

                //小组审核人待审核数量
                NpcMemberRole auditorRole = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());//小组审核人
                List<String> auditorUids = auditorRole.getNpcMembers().stream().filter(member -> member.getLevel().equals(npcMember.getLevel()) && ((member.getLevel().equals(LevelEnum.AREA.getValue()) && member.getArea().getUid().equals(npcMember.getArea().getUid())) || (member.getLevel().equals(LevelEnum.TOWN.getValue())) && member.getTown().getUid().equals(npcMember.getTown().getUid()))).map(NpcMember::getUid).collect(Collectors.toList());
                List<Performance> performanceList = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(auditorUids)) {//小组审核人存在的时候，继续往下统计
                    List<String> memberUids;//小组所有代表的uid
                    if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                        memberUids = npcMemberRepository.findByNpcMemberGroupUidAndIsDelFalse(npcMember.getNpcMemberGroup().getUid()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    } else {
                        memberUids = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(npcMember.getTown().getUid(), npcMember.getLevel()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    }
                    memberUids.removeAll(auditorUids);//将小组成员中的小组审核人过滤掉
//                    final List<String> members = memberUids;
                    performanceList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
                        List<Predicate> predicateList = new ArrayList<>();
                        predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                        predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                        predicateList.add(cb.isNull(root.get("status").as(int.class)));
                        predicateList.add(cb.isFalse(root.get("view").as(Boolean.class)));
                        predicateList.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                        if (level.equals(LevelEnum.TOWN.getValue())) {
                            predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                        }
                        CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                        in.value(memberUids);
                        predicateList.add(in);
                        return cb.and(predicateList.toArray(new Predicate[0]));
                    });
                }
                obj.put(MenuEnum.AUDIT_PERFORMANCE.toString(), performanceList.size());
            }
        }
        //代表
        body.setData(obj);
        return body;
    }

    @Override
    public RespBody getLevels(MobileUserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());//查询账号信息
        Set<AccountRole> accountRoles = account.getAccountRoles();
        List<LevelVo> levelVos = Lists.newArrayList();
        List<AccountRole> memberRoles = accountRoles.stream().filter(role -> role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(memberRoles)){//如果代表身份不为空
            for (NpcMember npcMember : account.getNpcMembers()) {
                String name = npcMember.getLevel().equals(LevelEnum.TOWN.getValue())?npcMember.getTown().getName():npcMember.getArea().getName();
                levelVos = npcMember.getNpcMemberRoles().stream().filter(role -> role.getIsMust()).map(role -> LevelVo.convert(role.getUid(),name+role.getName(),npcMember.getLevel())).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(levelVos)){//代表排除后，将后台管理员也排除掉
            levelVos = accountRoles.stream().filter(role -> (!role.getKeyword().equals(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword()))|| (!role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword()))).map(role -> LevelVo.convert(role.getUid(),role.getName(),LevelEnum.TOWN.getValue())).collect(Collectors.toList());
        }
        body.setData(levelVos);
        return body;
    }
}
