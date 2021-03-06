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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        if (account == null) {
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        if (account.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            JSONObject obj = new JSONObject();
            // 1 ??????  2 ?????????
            obj.put("status", 2);
            body.setData(obj);
            return body;
        }
        if (StringUtils.isEmpty(system)) {
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        //???????????????????????????
        Set<NpcMemberRole> roles = Sets.newHashSet();
        List<AccountRole> accountRoles = Lists.newArrayList();

        NpcMember npcMember = new NpcMember();
        //?????????json??????
        JSONObject object = new JSONObject();
        if (CollectionUtils.isEmpty(account.getNpcMembers())) {
            //?????????????????????????????????????????????????????????
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
            //???????????????????????????????????????????????????????????????
            npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
            if (npcMember == null) {
                object.put("ROLE", AccountRoleEnum.VOTER.getKeyword());
                object.put("ROLE_NAME", AccountRoleEnum.VOTER.getName());
            } else {
                roles = npcMember.getNpcMemberRoles();
                for (NpcMemberRole role : roles) {//?????????????????????
                    if (role.getIsMust()) {
                        object.put("ROLE", role.getKeyword());
                        object.put("ROLE_NAME", role.getName());
                        break;
                    }
                }
            }
        }

        boolean hasMobile = StringUtils.isNotBlank(account.getMobile());//????????????????????????
        if (!hasMobile) {
            body.setMessage("????????????");
            body.setStatus(HttpStatus.UNAUTHORIZED);
            return body;
        }
        SystemSetting systemSetting = this.getSystemSetting(userDetails, level);
        List<Menu> menus = Lists.newArrayList();//?????????????????????????????????
        List<Menu> systemMenus = menuRepository.findBySystemsUidAndEnabled(system, StatusEnum.ENABLED.getValue());//??????????????????????????????
        if (CollectionUtils.isNotEmpty(roles)) {
            for (NpcMemberRole role : roles) {//?????????????????????
                if (!role.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//????????????????????????
                if (!systemSetting.getPerformanceGroupAudit() && (role.getKeyword().equals(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword()) && level.equals(npcMember.getLevel())))
                    continue;//???????????????????????????????????????????????????????????????
                Set<Permission> permissions = role.getPermissions();
                if (permissions != null && !permissions.isEmpty()) {
                    for (Permission permission : permissions) {
                        if (!permission.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//????????????????????????
                        Set<Menu> miniAppMenus = permission.getMenus();//????????????????????????
                        if (miniAppMenus != null && !miniAppMenus.isEmpty()) {
                            miniAppMenus.retainAll(systemMenus);//????????????????????????????????????????????????????????????
                            for (Menu menu : miniAppMenus) {
                                if (!menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) continue;//?????????????????????
                                if (menu.getType().equals(StatusEnum.DISABLED.getValue())) continue;//????????????????????????????????????
                                menus.add(menu);
                            }
                        }
                    }
                }
            }
        }else if (CollectionUtils.isNotEmpty(accountRoles)){
            for (AccountRole role : accountRoles) {//?????????????????????
                if (!role.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//????????????????????????
                Set<Permission> permissions = role.getPermissions();
                if (permissions != null && !permissions.isEmpty()) {
                    for (Permission permission : permissions) {
                        if (!permission.getStatus().equals(StatusEnum.ENABLED.getValue())) continue;//????????????????????????
                        Set<Menu> miniAppMenus = permission.getMenus();//????????????????????????
                        if (miniAppMenus != null && !miniAppMenus.isEmpty()) {
                            miniAppMenus.retainAll(systemMenus);//????????????????????????????????????????????????????????????
                            for (Menu menu : miniAppMenus) {
                                if (!menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) continue;//?????????????????????
                                if (menu.getType().equals(StatusEnum.DISABLED.getValue())) continue;//????????????????????????????????????
                                menus.add(menu);
                            }
                        }
                    }
                }
            }
        }
        menus.sort(Comparator.comparing(Menu::getId));//???id??????
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
     * ????????????????????????????????????
     *
     * @param menus
     * @return
     */
    private List<MenuVo> dealChildren(List<Menu> menus) {
        List<MenuVo> menuVos = Lists.newArrayList();
        for (Menu menu : menus) {//?????????????????????
            if (menu.getParent() != null) {//????????????????????????????????????
                Boolean isHave = false;
                for (MenuVo menuVo : menuVos) {//?????????????????????
                    if (menuVo.getUid().equals(menu.getParent().getUid())) {
                        isHave = true;
                    }
                }
                if (!isHave) {
                    menuVos.add(MenuVo.convert(menu.getParent()));
                }
                for (MenuVo menuVo : menuVos) {//?????????????????????
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
        //?????????????????????????????????
        List<OpinionReply> opinionReplies = opinionReplayRepository.findAll((Specification<OpinionReply>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
//            predicateList.add(cb.equal(root.get("opinion").get("level").as(Byte.class), level));
            predicateList.add(cb.equal(root.get("opinion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (level.equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("opinion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            predicateList.add(cb.isFalse(root.get("view").as(Boolean.class)));
            predicateList.add(cb.equal(root.get("opinion").get("sender").get("uid").as(String.class), account.getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        });
        Set<Opinion> opinionSet = Sets.newHashSet();
        for (OpinionReply opinionReply : opinionReplies) {
            opinionSet.add(opinionReply.getOpinion());
        }
        obj.put(MenuEnum.MY_OPINION.toString(), opinionSet.size());

        if(npcMember != null) {
            //????????????????????????
            List<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
//                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.isFalse(root.get("view").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("receiver").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.RECEIVE_OPINION.toString(), opinions.size());

            //??????????????????????????????
            List<SuggestionReply> suggestionReplies = suggestionReplyRepository.findAll((Specification<SuggestionReply>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), level));
                predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                predicateList.add(cb.equal(root.get("suggestion").get("raiser").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.MY_SUGGESTION.toString(), suggestionReplies.size());

            //?????????????????????????????????
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

            // ????????????????????????
            List<NotificationViewDetail> notificationViewDetails = notificationViewDetailRepository.findAll((Specification<NotificationViewDetail>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("notification").get("level").as(Byte.class), level));
                predicateList.add(cb.isTrue(root.get("notification").get("published").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("notification").get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("notification").get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.isFalse(root.get("isRead").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("receiver").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.NOTIFICATION_INFO.toString(), notificationViewDetails.size());

            //????????????????????????
            List<Suggestion> suggestions = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));//todo ????????????
                predicateList.add(cb.isFalse(root.get("view").as(Boolean.class)));
//                predicateList.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.AUDIT_SUGGESTION.toString(), suggestions.size());

            //???????????????????????????
            List<News> news = newsRepository.findAll((Specification<News>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("status").as(Integer.class), NewsStatusEnum.UNDER_REVIEW.ordinal()));//todo ????????????????????????
                predicateList.add(cb.isFalse(root.get("view").as(Boolean.class)));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.AUDIT_NEWS.toString(), news.size());

            // ??????????????????
            List<Notification> notificationAuditors = notificationRepository.findAll((Specification<Notification>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicateList.add(cb.equal(root.get("status").as(Integer.class), NotificationStatusEnum.UNDER_REVIEW.ordinal()));//todo ????????????????????????
                predicateList.add(cb.equal(root.get("view").as(Boolean.class), false));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            obj.put(MenuEnum.AUDIT_NOTICE.toString(), notificationAuditors.size());

            //??????????????????????????????????????????

            if (npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList()).contains(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword())) {
                //???????????????uid
                List<String> generalAuditorUids;
                List<Performance> performanceGeneralList = Lists.newArrayList();
                SystemSetting systemSetting = this.getSystemSetting(userDetails, level);
                if (systemSetting.getPerformanceGroupAudit()) {//???????????????????????????????????????????????????
                    NpcMemberRole generalAuditorRole = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());
                    generalAuditorUids = generalAuditorRole.getNpcMembers().stream().filter(member -> member.getLevel().equals(npcMember.getLevel()) && ((member.getLevel().equals(LevelEnum.AREA.getValue()) && member.getArea().getUid().equals(npcMember.getArea().getUid())) || (member.getLevel().equals(LevelEnum.TOWN.getValue())) && member.getTown().getUid().equals(npcMember.getTown().getUid()))).map(NpcMember::getUid).collect(Collectors.toList());
                } else {//???????????????????????????????????????
                    if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                        generalAuditorUids = npcMemberRepository.findByTownUidAndLevelAndStatusAndIsDelFalse(npcMember.getTown().getUid(), npcMember.getLevel(), StatusEnum.ENABLED.getValue()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    } else {
                        generalAuditorUids = npcMemberRepository.findByAreaUidAndLevelAndStatusAndIsDelFalse(npcMember.getArea().getUid(), npcMember.getLevel(), StatusEnum.ENABLED.getValue()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    }
                }
                if (CollectionUtils.isNotEmpty(generalAuditorUids)) {
                    performanceGeneralList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
                        List<Predicate> predicateList = new ArrayList<>();
                        predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                        predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                        predicateList.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.SUBMITTED_AUDIT.getValue()));
                        predicateList.add(cb.isFalse(root.get("view").as(Boolean.class)));
                        predicateList.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
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

                //??????????????????????????????
                NpcMemberRole auditorRole = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());//???????????????
                List<String> auditorUids = auditorRole.getNpcMembers().stream().filter(member -> member.getLevel().equals(npcMember.getLevel()) && ((member.getLevel().equals(LevelEnum.AREA.getValue()) && member.getArea().getUid().equals(npcMember.getArea().getUid())) || (member.getLevel().equals(LevelEnum.TOWN.getValue())) && member.getTown().getUid().equals(npcMember.getTown().getUid()))).map(NpcMember::getUid).collect(Collectors.toList());
                List<Performance> performanceList = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(auditorUids)) {//???????????????????????????????????????????????????
                    List<String> memberUids;//?????????????????????uid
                    if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                        memberUids = npcMemberRepository.findByNpcMemberGroupUidAndIsDelFalse(npcMember.getNpcMemberGroup().getUid()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    } else {
                        memberUids = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(npcMember.getTown().getUid(), npcMember.getLevel()).stream().map(NpcMember::getUid).collect(Collectors.toList());
                    }
                    memberUids.removeAll(auditorUids);//?????????????????????????????????????????????
//                    final List<String> members = memberUids;
                    if (CollectionUtils.isNotEmpty(memberUids)) {
                        performanceList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
                            List<Predicate> predicateList = new ArrayList<>();
                            predicateList.add(cb.equal(root.get("level").as(Byte.class), level));
                            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                            predicateList.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.SUBMITTED_AUDIT.getValue()));
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
                }
                obj.put(MenuEnum.AUDIT_PERFORMANCE.toString(), performanceList.size());
            }
        }
        //??????
        body.setData(obj);
        return body;
    }

    @Override
    public RespBody getLevels(MobileUserDetailsImpl userDetails,Byte level) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());//??????????????????
        Set<AccountRole> accountRoles = account.getAccountRoles();
        List<LevelVo> levelVos = Lists.newArrayList();
        List<AccountRole> memberRoles = accountRoles.stream().filter(role -> role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(memberRoles)){//???????????????????????????
            for (NpcMember npcMember : account.getNpcMembers()) {
                String name = npcMember.getLevel().equals(LevelEnum.TOWN.getValue())?npcMember.getTown().getName():npcMember.getArea().getName();//?????????????????????????????????
                List<LevelVo> memberLevelVos = npcMember.getNpcMemberRoles().stream().filter(role -> role.getIsMust()).map(role -> LevelVo.convert(role.getUid(),name+role.getName(),npcMember.getLevel(),(byte)1,name)).collect(Collectors.toList());
                levelVos.addAll(memberLevelVos);
            }
        }
        if (CollectionUtils.isEmpty(levelVos)){//????????????????????????????????????????????????
            String name = account.getVoter().getTown().getName();//??????????????????????????????
            levelVos = accountRoles.stream().filter(role -> !(role.getKeyword().equals(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword())|| role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword())) || role.getKeyword().equals(AccountRoleEnum.GOVERNMENT.getKeyword()) || role.getKeyword().equals(AccountRoleEnum.UNIT.getKeyword())).map(role -> LevelVo.convert(role.getUid(),role.getName(), LevelEnum.TOWN.getValue(),(byte)2,name)).collect(Collectors.toList());
        }
        //????????????????????????????????????
        if (levelVos.size()>1){
            if (!levelVos.get(0).getLevel().equals(level)) {
                LevelVo levelVo = new LevelVo();
                levelVo = levelVos.get(0);
                levelVos.set(0,levelVos.get(1));
                levelVos.set(1,levelVo);
            }
        }
        body.setData(levelVos);
        return body;
    }
}
