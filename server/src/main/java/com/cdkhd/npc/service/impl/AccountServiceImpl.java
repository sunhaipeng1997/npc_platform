package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.AccountPageDto;
import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.entity.vo.AccountVo;
import com.cdkhd.npc.enums.LoginWayEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.VoterRepository;
import com.cdkhd.npc.repository.member_house.VillageRepository;
import com.cdkhd.npc.service.AccountService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    private VoterRepository voterRepository;

    private VillageRepository villageRepository;

    private final Environment env;


    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, VoterRepository voterRepository, VillageRepository villageRepository, Environment env) {
        this.accountRepository = accountRepository;
        this.voterRepository = voterRepository;
        this.villageRepository = villageRepository;
        this.env = env;
    }

    @Override
    public RespBody findAccount(AccountPageDto accountPageDto) {
        RespBody body = new RespBody();
        int begin = accountPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, accountPageDto.getSize(), Sort.Direction.fromString(accountPageDto.getDirection()), accountPageDto.getProperty());
        Page<Account> accountPage = accountRepository.findAll((Specification<Account>)(root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("voter").isNotNull());
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("loginWay").as(Byte.class), LoginWayEnum.LOGIN_WECHAT.getValue()));
//            predicate = cb.and(predicate, cb.equal(root.get("loginWay").as(Byte.class), (byte)2));
            if (StringUtils.isNotEmpty(accountPageDto.getRealname())){
                predicates.add(cb.like(root.get("voter").get("realname").as(String.class), "%" + accountPageDto.getRealname() + "%"));
            }
            if (StringUtils.isNotEmpty(accountPageDto.getMobile())){
                predicates.add(cb.equal(root.get("voter").get("mobile").as(String.class), "%" + accountPageDto.getMobile() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        PageVo<AccountVo> vo = new PageVo<>(accountPage, accountPageDto);
        List<AccountVo> accountVos = accountPage.getContent().stream().map(AccountVo :: convert).collect(Collectors.toList());
        vo.setContent(accountVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody changeStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(uid);
        if (account == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到此账号！");
            return body;
        }
        account.setStatus(status);
        accountRepository.saveAndFlush(account);
        return body;
    }


    @Override
    public RespBody getMyInfo(String uid) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(uid);
//        Account account = accountRepository.findByUid("751806ea2d4211ea8f3f0242ac170005");
        if (account == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到此账号！");
            return body;
        }
        AccountVo accountVo = AccountVo.convert(account);
        body.setData(accountVo);
        return body;
    }


    @Override
    public RespBody updateInfo(UserDetailsImpl userDetails, UserInfoDto userInfoDto) {
        RespBody body = new RespBody();
        if (userInfoDto.getGender() == null || StringUtils.isEmpty(userInfoDto.getVillageUid())){
            body.setMessage("用户信息不完整！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Voter voter = voterRepository.findByAccountUid(userDetails.getUid());
        final Integer updateTimes = Integer.parseInt(env.getProperty("miniapp.updatetimes"));
        if (voter.getUpdateInfo() >= updateTimes){
            body.setMessage("个人信息只允许修改 "+updateTimes+" 次！当前已修改 "+voter.getUpdateInfo()+" 次！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Village village = villageRepository.findByUid(userInfoDto.getVillageUid());
        Town town = village.getTown();
        Area area = town.getArea();
        voter.setGender(userInfoDto.getGender());
        voter.setVillage(village);
        voter.setTown(town);
        voter.setArea(area);
        voter.setUpdateInfo(voter.getUpdateInfo()+1);
        voterRepository.saveAndFlush(voter);
        body.setMessage("当前个人信息已修改 "+voter.getUpdateInfo()+" 次！还剩 "+(updateTimes-voter.getUpdateInfo())+" 次修改机会！");
        return body;
    }
}
