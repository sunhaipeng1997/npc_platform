package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.dto.AccountPageDto;
import com.cdkhd.npc.entity.vo.AccountVo;
import com.cdkhd.npc.enums.LoginWayEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.service.AccountService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
    public RespBody getMyInfo(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
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
}
