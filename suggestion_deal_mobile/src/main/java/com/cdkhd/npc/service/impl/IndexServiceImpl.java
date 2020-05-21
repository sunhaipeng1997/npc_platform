package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.LevelVo;
import com.cdkhd.npc.entity.vo.MenuVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SystemEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.AccountRoleRepository;
import com.cdkhd.npc.service.IndexService;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexServiceImpl implements IndexService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexServiceImpl.class);

    private AccountRepository accountRepository;
    private AccountRoleRepository accountRoleRepository;

    @Autowired
    public IndexServiceImpl(AccountRepository accountRepository, AccountRoleRepository accountRoleRepository) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
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
                LevelVo vo = LevelVo.convert(govUser.getUid(), areaTownName + role.getKeyword(),
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
                LevelVo vo = LevelVo.convert(unitUser.getUid(), areaTownName + role.getKeyword(),
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

        //当前身份为：代表/人大工委（审核人员）
        if (role.equals(AccountRoleEnum.NPC_MEMBER.getValue())) {
            NpcMember npcMember = NpcMemberUtil.getCurrentIden(level, account.getNpcMembers());
            List<MenuVo> voList = getMenuVos4Npc(npcMember);
            body.setData(voList);
            //当前身份为：政府
        } else if (role.equals(AccountRoleEnum.GOVERNMENT.getValue())) {
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.GOVERNMENT.toString());
            List<MenuVo> voList = getMenuVos4GovOrUnit(accountRole);
            body.setData(voList);
            //当前身份为：办理单位
        } else if (role.equals(AccountRoleEnum.UNIT.getValue())) {
            AccountRole accountRole = accountRoleRepository.findByKeyword(AccountRoleEnum.UNIT.toString());
            List<MenuVo> voList = getMenuVos4GovOrUnit(accountRole);
            body.setData(voList);
        }

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
        Set<Menu> menus = getMenusByPermission(permissions);

        return classifyMenu(menus);
    }

    /**
     * 为政府或办理单位获取菜单
     * @param role Account角色
     * @return 菜单Vo
     */
    private List<MenuVo> getMenuVos4GovOrUnit(AccountRole role) {
        //根据权限获取菜单
        Set<Menu> menus = getMenusByPermission(role.getPermissions());
        return classifyMenu(menus);
    }

    /**
     * 根据权限获取菜单
     * @param permissions 权限
     * @return 菜单
     */
    private Set<Menu> getMenusByPermission(Set<Permission> permissions) {
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
        return roleMenus;
    }

    /**
     * 将菜单根据父子关系分类
     * @param menus 菜单
     * @return 分类后的菜单Vo
     */
    private List<MenuVo> classifyMenu(Collection<Menu> menus) {
        List<MenuVo> voList = new ArrayList<>();
        Map<Menu, List<Menu>> map = new HashMap<>();

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
        for (Menu parent : map.keySet()) {
            MenuVo pVo = MenuVo.convert(parent);
            pVo.setChildren(map.get(parent).stream()
                    .map(MenuVo::convert)
                    .collect(Collectors.toList()));
            voList.add(pVo);
        }
        return voList;
    }
}
