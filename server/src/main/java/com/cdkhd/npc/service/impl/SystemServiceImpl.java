package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.AccountRole;
import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.entity.vo.SystemVo;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.SystemRepository;
import com.cdkhd.npc.service.SystemService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        for (AccountRole accountRole : accountRoleSet) {
            roleSystems.addAll(accountRole.getSystems());
        }
        List<Systems> systems = systemRepository.findByEnabledTrue();
        List<SystemVo> systemVos = systems.stream().map(SystemVo::convert).collect(Collectors.toList());
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
