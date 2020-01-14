package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.entity.vo.SystemVo;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.SystemRepository;
import com.cdkhd.npc.service.SystemService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
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
    public RespBody getSystemList() {
        RespBody body = new RespBody();
        List<Systems> systems = systemRepository.findByEnabledTrue();
        List<SystemVo> systemVos = systems.stream().map(SystemVo::convert).collect(Collectors.toList());
        body.setData(systemVos);
        return body;
    }

    @Override
    public RespBody cacheSystem(UserDetailsImpl userDetails, String systemId) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        Systems systems = systemRepository.findByUid(systemId);
        account.setSystems(systems);
        accountRepository.saveAndFlush(account);
        return body;
    }

    @Override
    public RespBody getCacheSystem(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        if (account.getSystems() != null) {
            body.setData(account.getSystems().getUrl());
        }
        return body;
    }
}
