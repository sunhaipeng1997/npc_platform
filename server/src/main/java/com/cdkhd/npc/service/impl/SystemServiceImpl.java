package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.entity.vo.SystemVo;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.SystemRepository;
import com.cdkhd.npc.service.SystemService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public RespBody getSystemList() {
        RespBody body = new RespBody();
        List<Systems> systems = systemRepository.findByEnabledTrue();
        List<SystemVo> systemVos = systems.stream().map(SystemVo::convert).collect(Collectors.toList());
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
    public RespBody getCacheSystem(String uid,Byte source) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(uid);
        if (account.getSystems() != null) {
            CommonVo commonVo = new CommonVo();
            commonVo.setUid(account.getSystems().getUid());
            if (source.equals(StatusEnum.ENABLED.getValue())) {
                commonVo.setName(account.getSystems().getUrl());
            }else{
                commonVo.setName(account.getSystems().getPagePath());
            }
            body.setData(commonVo);
        }
        return body;
    }
}
