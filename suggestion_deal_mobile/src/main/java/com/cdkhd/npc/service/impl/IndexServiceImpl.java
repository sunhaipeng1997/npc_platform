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
     * ?????????????????????????????????
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
            //?????????
            if (role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword())) {
                for (NpcMember npcMember : account.getNpcMembers()) {
                    //?????????????????????????????????
                    String areaTownName = npcMember.getLevel().equals(LevelEnum.TOWN.getValue()) ?
                            npcMember.getTown().getName() :
                            npcMember.getArea().getName();
                    //????????????????????????????????????LevelVo
                    List<LevelVo> memberLevelVos = npcMember.getNpcMemberRoles().stream()
                            .filter(NpcMemberRole::getIsMust)
                            .map(npcRole -> LevelVo.convert(npcRole.getUid(), areaTownName + npcRole.getName(),
                                    npcMember.getLevel(), AccountRoleEnum.NPC_MEMBER.getValue(), areaTownName))
                            .collect(Collectors.toList());
                    levelVos.addAll(memberLevelVos);
                }
            //?????????
            } else if (role.getKeyword().equals(AccountRoleEnum.GOVERNMENT.getKeyword())) {
                GovernmentUser govUser = account.getGovernmentUser();
                //?????????????????????????????????
                String areaTownName = govUser.getLevel().equals(LevelEnum.TOWN.getValue()) ?
                        govUser.getTown().getName() :
                        govUser.getArea().getName();
                //????????????LevelVo
                LevelVo vo = LevelVo.convert(govUser.getUid(), areaTownName + govUser.getGovernment().getName(),
                        govUser.getLevel(), AccountRoleEnum.GOVERNMENT.getValue(), areaTownName);
                levelVos.add(vo);
            //???????????????
            } else if (role.getKeyword().equals(AccountRoleEnum.UNIT.getKeyword())) {
                UnitUser unitUser = account.getUnitUser();
                //?????????????????????????????????
                String areaTownName = unitUser.getUnit().getLevel().equals(LevelEnum.TOWN.getValue()) ?
                        unitUser.getUnit().getTown().getName() :
                        unitUser.getUnit().getArea().getName();
                //????????????LevelVo
                LevelVo vo = LevelVo.convert(unitUser.getUid(), areaTownName + unitUser.getUnit().getName(),
                        unitUser.getUnit().getLevel(), AccountRoleEnum.UNIT.getValue(), areaTownName);
                levelVos.add(vo);
            }
        }

        //??????????????????/??????/??????????????????????????????
        if (CollectionUtils.isEmpty(levelVos)) {
            LOGGER.error("Account????????????????????????????????????????????????username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????????????????");
            return  body;
        }

        //??????
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
     * ?????????????????????????????????
     * @param userDetails ????????????
     * @param role Account???????????????
     * @param level ????????????
     * @return ??????
     */
    @Override
    public RespBody getMenus(MobileUserDetailsImpl userDetails, Byte role, Byte level) {
        RespBody<List<MenuVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (account.getVoter() == null) {
            LOGGER.error("??????????????????????????????????????????Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????");
            return body;
        }

        List<MenuVo> voList;
        //????????????????????????/??????????????????????????????
        if (role.equals(AccountRoleEnum.NPC_MEMBER.getValue())) {
            NpcMember npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
            voList = getMenuVos4Npc(npcMember);
            //????????????????????????
        } else if (role.equals(AccountRoleEnum.GOVERNMENT.getValue())) {
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.GOVERNMENT.name());
            voList = getMenuVos4GovOrUnit(accountRole);
            //??????????????????????????????
        } else if (role.equals(AccountRoleEnum.UNIT.getValue())) {
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.UNIT.name());
            voList = getMenuVos4GovOrUnit(accountRole);
        } else {
            LOGGER.error("????????????????????????????????????????????????????????????AccountRoleEnum.value???{}", role);
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????role???????????????????????????");
            return body;
        }

        body.setData(voList);
        return body;
    }

    /**
     * ????????????????????????????????????????????????????????????
     * @param userDetails ????????????
     * @param level ????????????
     * @return ??????
     */
    @Override
    public RespBody countUnRead(MobileUserDetailsImpl userDetails, Byte level) {
        RespBody<JSONObject> body = new RespBody<>();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
        JSONObject jsonObject = new JSONObject();

        //??????
        if (npcMember != null){
            //???????????????????????????????????????
            List<Suggestion> suggestions = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicateList.add(cb.isFalse(root.get("npcView").as(Boolean.class)));
                predicateList.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                return cb.and(predicateList.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.SUGGESTION_COMMITTED.toString(), suggestions.size());

            //???????????????????????????????????????
            List<Suggestion> notViewDoneSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));
                predicates.add(cb.isFalse(root.get("doneView").as(Boolean.class)));  //doneView?????????false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.SUGGESTION_DONE.toString(), notViewDoneSugList.size());

            //??????????????????????????????
            List<Suggestion> canSecondSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.notEqual(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));  //?????????????????????
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));  //???????????????
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));  //????????????
                if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));  //????????????
                }
                predicates.add(cb.or(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()),
                        cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue())));  //?????????????????????
                List<Seconded> secondeds = secondedRepository.findByNpcMemberUid(npcMember.getUid());//???????????????????????????
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(secondeds)) {
                    Set<String> sugUids = secondeds.stream().map(seconded -> seconded.getSuggestion().getUid()).collect(Collectors.toSet());
                    predicates.add(root.get("uid").in(sugUids).not());
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.OTHERS_SUGGESTIONS.toString(), canSecondSugList.size());

            //?????????????????????????????????????????????????????????
            List<Suggestion> notViewSecondDoneSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                Join<Suggestion, Seconded> join = root.join("secondedSet", JoinType.LEFT);//?????????????????????????????????
                //root.get  ??????suggestion?????????
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));  //????????????
                // join.get  ??????seconded?????????
                predicates.add(cb.equal(join.get("npcMember").get("uid").as(String.class), npcMember.getUid()));  //????????????????????????
                predicates.add(cb.isFalse(join.get("view").as(Boolean.class)));
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.SECONDED_SUGGESTIONS_COMPLETED.toString(), notViewSecondDoneSugList.size());

            //?????????????????? ??????????????????????????????
            List<Suggestion> toBeAuditedSugList = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class), level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));//todo ????????????
                predicates.add(cb.isFalse(root.get("view").as(Boolean.class)));
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.WAIT_AUDIT_SUGGESTIONS.toString(), toBeAuditedSugList.size());
        }

        //??????
        GovernmentUser governmentUser = account.getGovernmentUser();
        if (governmentUser != null){
            //??????????????????
            List<Suggestion> waitConveySugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView?????????false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.WAIT_CONVEY_SUGGESTIONS.toString(), waitConveySugList.size());

            //?????????????????????
            List<DelaySuggestion> delaySugList = delaySuggestionRepository.findAll((Specification<DelaySuggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));
                predicates.add(cb.isNull(root.get("accept")));//???????????????
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView?????????false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.APPLY_DELAY_SUGGESTIONS.toString(), delaySugList.size());


            //???????????????????????????
            List<ConveyProcess> adjustSugList = conveyProcessRepository.findAll((Specification<ConveyProcess>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
//                predicates.add(cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//????????????????????????
                predicates.add(cb.equal(root.get("status").as(Byte.class), ConveyStatusEnum.CONVEY_FAILED.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView?????????false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.APPLY_ADJUST_SUGGESTIONS.toString(), adjustSugList.size());


            //??????????????????
            List<Suggestion> dealingSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));
//                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView?????????false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.GOV_DEALING_SUGGESTIONS.toString(), dealingSugList.size());

            //?????????????????????
            List<Suggestion> finishSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView?????????false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.GOV_FINISHED_SUGGESTIONS.toString(), finishSugList.size());

            //???????????????
            List<Suggestion> completedSugList = suggestionRepository.findAll((Specification<Suggestion>)(root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("level").as(Byte.class),level));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class),governmentUser.getArea().getUid()));
                if (level.equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class),governmentUser.getTown().getUid()));
                }
                predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));
                predicates.add(cb.isFalse(root.get("govView").as(Boolean.class)));  //doneView?????????false
                return cb.and(predicates.toArray(new Predicate[0]));
            });
            jsonObject.put(MenuEnum.GOV_COMPLETED_SUGGESTIONS.toString(), completedSugList.size());
        }

        //????????????
        UnitUser unitUser = account.getUnitUser();
        if (Objects.nonNull(unitUser)) {
            countUnread4Unit(unitUser, jsonObject);
        }

        body.setData(jsonObject);
        return body;
    }

    /**
     * ?????????????????????Vo
     * @param npcMember ??????
     * @return ??????Vo
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
        //????????????????????????
        List<Menu> menus = getMenusByPermission(permissions);

        return classifyMenu(menus);
    }

    /**
     * ????????????????????????????????????
     * @param role Account??????
     * @return ??????Vo
     */
    private List<MenuVo> getMenuVos4GovOrUnit(AccountRole role) {
        //????????????????????????
        List<Menu> menus = getMenusByPermission(role.getPermissions());
        return classifyMenu(menus);
    }

    /**
     * ????????????????????????
     * @param permissions ??????
     * @return ??????
     */
    private List<Menu> getMenusByPermission(Set<Permission> permissions) {
        Set<Menu> roleMenus = permissions.stream()
                .filter(permission -> permission.getStatus().equals(StatusEnum.ENABLED.getValue())) //????????????
                .map(Permission::getMenus)
                .reduce(new HashSet<>(), (ms, s) -> {
                    ms.addAll(s);
                    return ms;
                });
        roleMenus = roleMenus.stream()
                .filter(menu -> menu.getEnabled().equals(StatusEnum.ENABLED.getValue())) //????????????
                .filter(menu -> menu.getType().equals((byte)1)) //???????????????
                .filter(menu -> menu.getSystems().getName().equals(SystemEnum.SUGGESTION.getName())) //?????????????????????????????????
                .collect(Collectors.toSet());
        //??????id???????????????
        List<Menu> sortedMenus = new ArrayList<>(roleMenus);
        sortedMenus.sort(Comparator.comparing(Menu::getId));
        return sortedMenus;
    }

    /**
     * ?????????????????????????????????
     * @param menus ??????
     * @return ??????????????????Vo
     */
    private List<MenuVo> classifyMenu(Collection<Menu> menus) {
        List<MenuVo> voList = new ArrayList<>();
        //??????LinkedHashMap?????????????????????
        Map<Menu, List<Menu>> map = new LinkedHashMap<>();

        //????????????????????????
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

        //????????????????????????MenuVo
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
     * ??????????????????????????????????????????
     *
     * @param unitUser ??????????????????
     * @param obj ????????????????????? JSONObject ??????
     */
    private void countUnread4Unit(UnitUser unitUser, JSONObject obj) {
        //?????????????????????
        List<ConveyProcess> toDeal = conveyProcessRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //???????????????
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //????????????????????????????????????
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.TRANSFERRED_UNIT.getValue()));
            //?????????????????????????????????
            predicateList.add(cb.equal(root.get("status").as(Byte.class), ConveyStatusEnum.CONVEYING.getValue()));
            //???????????????????????????
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            //???????????????
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.WAIT_DEAL_SUGGESTIONS.name(), toDeal.size());

        //????????????????????????
        List<UnitSuggestion> inDealing = unitSuggestionRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //???????????????
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //??????????????????????????????????????????????????????????????????????????????suggestion??????????????????????????????????????????????????????
            /*predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.HANDLING.getValue()));*/
            //unitSug???finish???false?????????
            predicateList.add(cb.isFalse(root.get("finish").as(Boolean.class)));
            //?????????????????????
            predicateList.add(cb.equal(root.get("unitUser").get("uid").as(String.class),
                    unitUser.getUid()));
            //???????????????
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.DEALING_SUGGESTIONS.name(), inDealing.size());

        //????????????????????????
        List<UnitSuggestion> done = unitSuggestionRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //???????????????
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //????????????????????????????????????
            Predicate handled = cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue());
            Predicate dealing = cb.equal(root.get("suggestion").get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue());
            Predicate or = cb.or(dealing, handled);
            predicateList.add(or);
            //unitSug???finish???true?????????
            predicateList.add(cb.isTrue(root.get("finish").as(Boolean.class)));
            //unitSug?????????????????????Suggestion????????????????????????????????????????????????
            predicateList.add(cb.equal(root.get("dealTimes").as(Integer.class),
                    root.get("suggestion").get("dealTimes").as(Integer.class)));
            //???????????????????????????
            predicateList.add(cb.equal(root.get("unitUser").get("uid").as(String.class),
                    unitUser.getUid()));
            //???????????????
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.DEAL_DONE_SUGGESTIONS.name(), done.size());

        //????????????????????????
        List<UnitSuggestion> complete = unitSuggestionRepository.findAll(((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //???????????????
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //????????????????????????
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.ACCOMPLISHED.getValue()));
            //unitSug???finish???true
            predicateList.add(cb.isTrue(root.get("finish").as(Boolean.class)));
            //unitSug?????????????????????Suggestion????????????????????????????????????????????????
            predicateList.add(cb.equal(root.get("dealTimes").as(Integer.class),
                    root.get("suggestion").get("dealTimes").as(Integer.class)));
            //?????????????????????
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            //???????????????
            predicateList.add(cb.isFalse(root.get("unitView").as(Boolean.class)));

            return cb.and(predicateList.toArray(new Predicate[0]));
        }));
        obj.put(MenuEnum.DEAL_COMPLETED_SUGGESTIONS.name(), complete.size());
    }
}
