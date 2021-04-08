package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.SystemVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.SystemRepository;
import com.cdkhd.npc.service.SystemService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemServiceImpl implements SystemService {
    private Environment env;

    private SystemRepository systemRepository;

    private AccountRepository accountRepository;

    @Autowired
    public SystemServiceImpl(Environment env, SystemRepository systemRepository, AccountRepository accountRepository) {
        this.env = env;
        this.systemRepository = systemRepository;
        this.accountRepository = accountRepository;
    }


    @Override
    public RespBody getSystemList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        Set<AccountRole> accountRoleSet = account.getAccountRoles();
        Set<Systems> roleSystems = Sets.newHashSet();
        //获取当前镇、区下可用系统
        Set<Systems> areaSystems = Sets.newHashSet();
        Boolean isResident = false;
        for (AccountRole accountRole : accountRoleSet) {
            if (accountRole.getKeyword().equals(AccountRoleEnum.VOTER.getKeyword()) && accountRoleSet.size() == 1){
                roleSystems.addAll(accountRole.getSystems());//如果仅仅是选民，那么就获取选民应该展示的系统列表
                areaSystems.addAll(userDetails.getTown().getSystems());
                isResident = true;
            }
        }
        if (!isResident){//不是居民的话
            for (AccountRole accountRole : accountRoleSet) {
                roleSystems.addAll(accountRole.getSystems());
                if (accountRole.getKeyword().equals(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword())) {
                    BackgroundAdmin backgroundAdmin = account.getBackgroundAdmin();
                    if (backgroundAdmin.getLevel().equals(LevelEnum.AREA.getValue())){
                        areaSystems.addAll(backgroundAdmin.getArea().getSystems());
                    }else{
                        areaSystems.addAll(backgroundAdmin.getTown().getSystems());
                    }
                }else if (accountRole.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword())) {
                    Set<NpcMember> npcMembers = account.getNpcMembers();
                    for (NpcMember npcMember : npcMembers) {
                        if (!npcMember.getIsDel() && npcMember.getStatus().equals(StatusEnum.ENABLED.getValue())) {
                            if (npcMember.getLevel().equals(LevelEnum.AREA.getValue())) {
                                areaSystems.addAll(npcMember.getArea().getSystems());
                            } else {
                                areaSystems.addAll(npcMember.getTown().getSystems());
                            }
                        }
                    }
                }else if (accountRole.getKeyword().equals(AccountRoleEnum.GOVERNMENT.getKeyword())) {
                    GovernmentUser governmentUser = account.getGovernmentUser();
                    if (governmentUser.getStatus().equals(StatusEnum.ENABLED.getValue())) {
                        if (governmentUser.getLevel().equals(LevelEnum.AREA.getValue())) {
                            areaSystems.addAll(governmentUser.getArea().getSystems());
                        } else {
                            areaSystems.addAll(governmentUser.getTown().getSystems());
                        }
                    }
                }else if (accountRole.getKeyword().equals(AccountRoleEnum.UNIT.getKeyword())) {
                    UnitUser unitUser = account.getUnitUser();
                    if (!unitUser.getIsDel() && unitUser.getStatus().equals(StatusEnum.ENABLED.getValue()) && !unitUser.getUnit().getIsDel() && !unitUser.getUnit().getStatus().equals(StatusEnum.ENABLED.getValue())) {
                        if (unitUser.getUnit().getLevel().equals(LevelEnum.AREA.getValue())) {
                            areaSystems.addAll(unitUser.getUnit().getArea().getSystems());
                        } else {
                            areaSystems.addAll(unitUser.getUnit().getTown().getSystems());
                        }
                    }
                }
            }
        }
        //获取所有可用系统
        List<SystemVo> systemVos = areaSystems.stream().filter(Systems::getEnabled).map(SystemVo::convert).collect(Collectors.toList());
        systemVos.sort(Comparator.comparing(SystemVo::getSequence));
        for (SystemVo systemVo : systemVos) {
            for (Systems roleSystem : roleSystems) {
                if (systemVo.getUid().equals(roleSystem.getUid())){
                    systemVo.setCanUse(true);
                    break;
                }
            }
        }
        body.setData(systemVos);
        return body;
    }

    @Override
    public RespBody cacheSystem(String uid, String systemId) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(uid);
        Systems systems = systemRepository.findByUid(systemId);
        account.setSystems(systems);
        accountRepository.saveAndFlush(account);
        return body;
    }

    @Override
    public RespBody getCacheSystem(String uid, Byte source) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(uid);
        if (account.getSystems() != null) {
            SystemVo systemVo = new SystemVo();
            systemVo.setUid(account.getSystems().getUid());
            if (source.equals(StatusEnum.ENABLED.getValue())) {
                systemVo.setName(account.getSystems().getName());
                systemVo.setUrl(account.getSystems().getUrl());
            } else {
                systemVo.setName(account.getSystems().getPagePath());
            }
            body.setData(systemVo);
        }
        return body;
    }
}
