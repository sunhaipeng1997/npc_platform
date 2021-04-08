package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.LevelVo;
import com.cdkhd.npc.entity.vo.MenuVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.AccountRoleRepository;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.base.DelaySuggestionRepository;
import com.cdkhd.npc.repository.member_house.SuggestionReplyRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.SecondedRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitSuggestionRepository;
import com.cdkhd.npc.service.IndexService;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexServiceImpl implements IndexService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexServiceImpl.class);

    private AccountRepository accountRepository;
    private AccountRoleRepository accountRoleRepository;
    private SuggestionRepository suggestionRepository;
    private SecondedRepository secondedRepository;
    private SuggestionReplyRepository suggestionReplyRepository;
    private DelaySuggestionRepository delaySuggestionRepository;
    private ConveyProcessRepository conveyProcessRepository;
    private UnitSuggestionRepository unitSuggestionRepository;

    @Autowired
    public IndexServiceImpl(AccountRepository accountRepository, AccountRoleRepository accountRoleRepository, SuggestionRepository suggestionRepository, SecondedRepository secondedRepository, SuggestionReplyRepository suggestionReplyRepository, DelaySuggestionRepository delaySuggestionRepository, ConveyProcessRepository conveyProcessRepository, UnitSuggestionRepository unitSuggestionRepository) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.suggestionRepository = suggestionRepository;
        this.secondedRepository = secondedRepository;
        this.suggestionReplyRepository = suggestionReplyRepository;
        this.delaySuggestionRepository = delaySuggestionRepository;
        this.conveyProcessRepository = conveyProcessRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
    }

    /**
     * 获取当前用户的身份信息
     * @param userDetails
     * @return
     */
    @Override
    public RespBody getIdentityInfo(MobileUserDetailsImpl userDetails) {
        RespBody<List<LevelVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        Set<AccountRole> roles = account.getAccountRoles();
        List<LevelVo> levelVos = new ArrayList<>();
        for (AccountRole role : roles) {
            //是代表
            if (role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword())) {
                for (NpcMember npcMember : account.getNpcMembers()) {
                    //获取代表所在区镇的名称
                    String areaTownName = npcMember.getLevel().equals(LevelEnum.TOWN.getValue()) ?
                            npcMember.getTown().getName() :
                            npcMember.getArea().getName();
                    //将代表的每个角色生成一个LevelVo
                    List<LevelVo> memberLevelVos = npcMember.getNpcMemberRoles().stream()
                            .filter(NpcMemberRole::getIsMust)
                            .map(npcRole -> LevelVo.convert(npcRole.getUid(), areaTownName + npcRole.getName(),
                                    npcMember.getLevel(), AccountRoleEnum.NPC_MEMBER.getValue(), areaTownName))
                            .collect(Collectors.toList());
                    levelVos.addAll(memberLevelVos);
                }
            //是政府
            } else if (role.getKeyword().equals(AccountRoleEnum.GOVERNMENT.getKeyword())) {
                GovernmentUser govUser = account.getGovernmentUser();
                //获取政府所在区镇的名称
                String areaTownName = govUser.getLevel().equals(LevelEnum.TOWN.getValue()) ?
                        govUser.getTown().getName() :
                        govUser.getArea().getName();
                //生成一个LevelVo
                LevelVo vo = LevelVo.convert(govUser.getUid(), areaTownName + govUser.getGovernment().getName(),
                        govUser.getLevel(), AccountRoleEnum.GOVERNMENT.getValue(), areaTownName);
                levelVos.add(vo);
            //是办理单位
            } else if (role.getKeyword().equals(AccountRoleEnum.UNIT.getKeyword())) {
                UnitUser unitUser = account.getUnitUser();
                //获取单位所在区镇的名称
                String areaTownName = unitUser.getUnit().getLevel().equals(LevelEnum.TOWN.getValue()) ?
                        unitUser.getUnit().getTown().getName() :
                        unitUser.getUnit().getArea().getName();
                //生成一个LevelVo
                LevelVo vo = LevelVo.convert(unitUser.getUid(), areaTownName + unitUser.getUnit().getName(),
                        unitUser.getUnit().getLevel(), AccountRoleEnum.UNIT.getValue(), areaTownName);
                levelVos.add(vo);
            }
        }

        //未获取到代表/政府/办理单位身份，则报错
        if (CollectionUtils.isEmpty(levelVos)) {
            LOGGER.error("Account身份有误，无法进入建议办理系统，username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("选民无法进入系统，请在后台更改身份");
            return  body;
        }

        //排序
        levelVos.sort((level1, level2) -> {
            if (level1.getLevel() > level2.getLevel()) {
                return -1;
            }

            if (level1.getLevel() < level2.getLevel()) {
                return 1;
            }

            return 0;
        });

        body.setData(levelVos);
        return body;
    }

    /**
     * 获取所选当前身份的菜单
     * @param userDetails 当前用户
     * @param role Account角色枚举值
     * @param level 区域等级
     * @return 菜单
     */
    @Override
    public RespBody getMenus(MobileUserDetailsImpl userDetails, Byte role, Byte level) {
        RespBody<List<MenuVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (account.getVoter() == null) {
            LOGGER.error("用户未未注册，无法获取菜单。Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("用户未注册");
            return body;
        }

        List<MenuVo> voList;
        //当前身份为：代表/人大工委（审核人员）
        if (role.equals(AccountRoleEnum.NPC_MEMBER.getValue())) {
            NpcMember npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
            voList = getMenuVos4Npc(npcMember);
            //当前身份为：政府
        } else if (role.equals(AccountRoleEnum.GOVERNMENT.getValue())) {
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.GOVERNMENT.name());
            voList = getMenuVos4GovOrUnit(accountRole);
            //当前身份为：办理单位
        } else if (role.equals(AccountRoleEnum.UNIT.getValue())) {
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.UNIT.name());
            voList = getMenuVos4GovOrUnit(accountRole);
        } else {
            LOGGER.error("无法获取角色相关的小程序菜单，角色枚举值AccountRoleEnum.value：{}", role);
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("角色值参数role错误，获取菜单失败");
            return body;
        }

        body.setData(voList);
        return body;
    }

    /**
     * 获取所选当前身份的菜单对应的未读内容数量
     * @param userDetails 当前用户
     * @param level 区域等级
     * @return 数量
     */
    @Override
    public RespBody countUnRead(MobileUserDetailsImpl userDetails, Byte level) {
        RespBody<JSONObject> body = new RespBody<>();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
        JSONObject jsonObject = new JSONObject();

        //代表
        if (npcMember != null){
            //已审核且代表未读的建议数量
            List<Suggestion> suggestions = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicateList.add(cb.isFalse(root.get("npcView").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.SUGGESTION_COMMITTED.toString(), suggestions.size());

            //已办完且代表未读的建议数量
            List<Suggestion> notViewDoneSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));
                predicates.add(cb.isFalse(root.get("doneView").as(Boolean.class)));  //doneView字段为false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.SUGGESTION_DONE.toString(), notViewDoneSugList.size());

            //代表能附议的建议数量
            List<Suggestion> canSecondSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.notEqual(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));  //剔除我自己提的
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));  //与我同级别
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));  //与我同区
                if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));  //与我同镇
                }
                predicates.add(cb.or(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()),
                        cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue())));  //待审核或待转办
                List<Seconded> secondeds = secondedRepository.findByNpcMemberUid(npcMember.getUid());//剔除已经附议的建议
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(secondeds)) {
                    Set<String> sugUids = secondeds.stream().map(seconded -> seconded.getSuggestion().getUid()).collect(Collectors.toSet());
                    predicates.add(root.get("uid").in(sugUids).not());
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.OTHERS_SUGGESTIONS.toString(), canSecondSugList.size());

            //代表附议办结的且自己还未查看的建议数量
            List<Suggestion> notViewSecondDoneSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                Join<Suggestion, Seconded> join = root.join("secondedSet", JoinType.LEFT);//左连接，把附议表加进来
                //root.get  表示suggestion的字段
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));  //建议办结
                // join.get  表示seconded的字段
                predicates.add(cb.equal(join.get("npcMember").get("uid").as(String.class), npcMember.getUid()));  //该代表提出的附议
                predicates.add(cb.isFalse(join.get("view").as(Boolean.class)));
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.SECONDED_SUGGESTIONS_COMPLETED.toString(), notViewSecondDoneSugList.size());

            //待审核的建议 只有审核人员有此菜单
            List<Suggestion> toBeAuditedSugList = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class), level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));//todo 建议状态
                predicates.add(cb.isFalse(root.get("view").as(Boolean.class)));
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.WAIT_AUDIT_SUGGESTIONS.toString(), toBeAuditedSugList.size());
        }

        //政府
        GovernmentUser governmentUser = account.getGovernmentUser();
        if (governmentUser != null){
            //待转办的建议
            List<Suggestion> waitConveySugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView字段为false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.WAIT_CONVEY_SUGGESTIONS.toString(), waitConveySugList.size());

            //申请延期的建议
            List<DelaySuggestion> delaySugList = delaySuggestionRepository.findAll((Specification<DelaySuggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));
                predicates.add(cb.isNull(root.get("accept")));//延期未处理
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView字段为false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.APPLY_DELAY_SUGGESTIONS.toString(), delaySugList.size());


            //申请调整单位的建议
            List<ConveyProcess> adjustSugList = conveyProcessRepository.findAll((Specification<ConveyProcess>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
//                predicates.add(cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//建议状态为待转交
                predicates.add(cb.equal(root.get("status").as(Byte.class), ConveyStatusEnum.CONVEY_FAILED.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView字段为false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.APPLY_ADJUST_SUGGESTIONS.toString(), adjustSugList.size());


            //办理中的建议
            List<Suggestion> dealingSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));
//                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView字段为false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.GOV_DEALING_SUGGESTIONS.toString(), dealingSugList.size());

            //办理完成的建议
            List<Suggestion> finishSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView字段为false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.GOV_FINISHED_SUGGESTIONS.toString(), finishSugList.size());

            //办结的建议
            List<Suggestion> completedSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView字段为false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.GOV_COMPLETED_SUGGESTIONS.toString(), completedSugList.size());
        }

        //办理单位
        UnitUser unitUser = account.getUnitUser();
        if (Objects.nonNull(unitUser)) {
            countUnread4Unit(unitUser, jsonObject);
        }

        body.setData(jsonObject);
        return body;
    }

    /**
     * 为代表获取菜单Vo
     * @param npcMember 代表
     * @return 菜单Vo
     */
    private List<MenuVo> getMenuVos4Npc(NpcMember npcMember) {
        Set<NpcMemberRole> roles = npcMember.getNpcMemberRoles();
        Set<Permission> permissions = roles.stream()
                .filter(role -> role.getStatus().equals(StatusEnum.ENABLED.getValue()))
                .map(NpcMemberRole::getPermissions)
                .reduce(new HashSet<>(), (ps, s) -> {
                    ps.addAll(s);
                    return ps;
                });
        //根据权限获取菜单
        List<Menu> menus = getMenusByPermission(permissions);

        return classifyMenu(menus);
    }

    /**
     * 为政府或办理单位获取菜单
     * @param role Account角色
     * @return 菜单Vo
     */
    private List<MenuVo> getMenuVos4GovOrUnit(AccountRole role) {
        //根据权限获取菜单
        List<Menu> menus = getMenusByPermission(role.getPermissions());
        return classifyMenu(menus);
    }

    /**
     * 根据权限获取菜单
     * @param permissions 权限
     * @return 菜单
     */
    private List<Menu> getMenusByPermission(Set<Permission> permissions) {
        Set<Menu> roleMenus = permissions.stream()
                .filter(permission -> permission.getStatus().equals(StatusEnum.ENABLED.getValue())) //有效权限
                .map(Permission::getMenus)
                .reduce(new HashSet<>(), (ms, s) -> {
                    ms.addAll(s);
                    return ms;
                });
        roleMenus = roleMenus.stream()
                .filter(menu -> menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) //有效菜单
                .filter(menu -> menu.getType().equals((byte)1)) //小程序菜单
                .filter(menu -> menu.getSystems().getName().equals(SystemEnum.SUGGESTION.getName())) //当前建议办理系统的菜单
                .collect(Collectors.toSet());
        //根据id将菜单排序
        List<Menu> sortedMenus = new ArrayList<>(roleMenus);
        sortedMenus.sort(Comparator.comparing(Menu::getId));
        return sortedMenus;
    }

    /**
     * 将菜单根据父子关系分类
     * @param menus 菜单
     * @return 分类后的菜单Vo
     */
    private List<MenuVo> classifyMenu(Collection<Menu> menus) {
        List<MenuVo> voList = new ArrayList<>();
        //使用LinkedHashMap避乱父菜单乱序
        Map<Menu, List<Menu>> map = new LinkedHashMap<>();

        //获取菜单父子关系
        for (Menu menu : menus) {
            if (menu.getParent() == null) {
                map.put(menu, new ArrayList<>());
            } else {
                if (!map.containsKey(menu.getParent())) {
                    map.put(menu.getParent(), new ArrayList<>());
                }
                map.get(menu.getParent()).add(menu);
            }
        }

        //根据父子关系生成MenuVo
        for (Map.Entry<Menu, List<Menu>> entry : map.entrySet()) {
            MenuVo pVo = MenuVo.convert(entry.getKey());
            pVo.setChildren(entry.getValue().stream()
                    .map(MenuVo::convert)
                    .collect(Collectors.toList()));
            voList.add(pVo);
        }
        return voList;
    }

    /**
     * 为办理单位统计菜单的未读数量
     *
     * @param unitUser 办理单位人员
     * @param obj 保存未读数量的 JSONObject 对象
     */
    private void countUnread4Unit(UnitUser unitUser, JSONObject obj) {
        //未读的待办建议
        List<ConveyProcess> toDeal = conveyProcessRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为已转交办理单位
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.TRANSFERRED_UNIT.getValue()));
            //转办过程的状态为转办中
            predicateList.add(cb.equal(root.get("status").as(Byte.class), ConveyStatusEnum.CONVEYING.getValue()));
            //建议转交给当前单位
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            //单位未查看
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.WAIT_DEAL_SUGGESTIONS.name(), toDeal.size());

        //未读的办理中建议
        List<UnitSuggestion> inDealing = unitSuggestionRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为办理中（如果有另外的单位还未接受转办的话，suggestion的状态就不是办理中，故注释掉这一行）
            /*predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.HANDLING.getValue()));*/
            //unitSug的finish为false未办完
            predicateList.add(cb.isFalse(root.get("finish").as(Boolean.class)));
            //当前单位的建议
            predicateList.add(cb.equal(root.get("unitUser").get("uid").as(String.class),
                    unitUser.getUid()));
            //单位未查看
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.DEALING_SUGGESTIONS.name(), inDealing.size());

        //未读的已办完建议
        List<UnitSuggestion> done = unitSuggestionRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为已办完或办理中
            Predicate handled = cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue());
            Predicate dealing = cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue());
            Predicate or = cb.or(dealing, handled);
            predicateList.add(or);
            //unitSug的finish为true已办完
            predicateList.add(cb.isTrue(root.get("finish").as(Boolean.class)));
            //unitSug的办理次数要与Suggestion的次数相同，只查出当前批次的办理
            predicateList.add(cb.equal(root.get("dealTimes").as(Integer.class),
                    root.get("suggestion").get("dealTimes").as(Integer.class)));
            //当前单位人员的建议
            predicateList.add(cb.equal(root.get("unitUser").get("uid").as(String.class),
                    unitUser.getUid()));
            //单位未查看
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.DEAL_DONE_SUGGESTIONS.name(), done.size());

        //未读的已办结建议
        List<UnitSuggestion> complete = unitSuggestionRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为已办结
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.ACCOMPLISHED.getValue()));
            //unitSug的finish为true
            predicateList.add(cb.isTrue(root.get("finish").as(Boolean.class)));
            //unitSug的办理次数要与Suggestion的次数相同，只查出当前批次的办理
            predicateList.add(cb.equal(root.get("dealTimes").as(Integer.class),
                    root.get("suggestion").get("dealTimes").as(Integer.class)));
            //当前单位的建议
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            //单位未查看
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.DEAL_COMPLETED_SUGGESTIONS.name(), complete.size());
    }
}
