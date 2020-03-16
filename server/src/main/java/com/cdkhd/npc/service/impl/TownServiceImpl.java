package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.TownAddDto;
import com.cdkhd.npc.entity.dto.TownPageDto;
import com.cdkhd.npc.entity.vo.TownDetailsVo;
import com.cdkhd.npc.entity.vo.TownPageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.TownService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Sets;
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
import java.util.stream.Collectors;

@Service
public class TownServiceImpl implements TownService {

    private final TownRepository townRepository;

    private final AccountRepository accountRepository;

    private final LoginUPRepository loginUPRepository;

    private final AccountRoleRepository accountRoleRepository;

    private final VoterRepository voterRepository;

    private final SystemSettingRepository systemSettingRepository;

    private final BackgroundAdminRepository backgroundAdminRepository;


    @Autowired
    public TownServiceImpl(TownRepository townRepository, AccountRepository accountRepository, LoginUPRepository loginUPRepository, AccountRoleRepository accountRoleRepository, VoterRepository voterRepository, SystemSettingRepository systemSettingRepository, BackgroundAdminRepository backgroundAdminRepository) {
        this.townRepository = townRepository;
        this.accountRepository = accountRepository;
        this.loginUPRepository = loginUPRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.voterRepository = voterRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.backgroundAdminRepository = backgroundAdminRepository;
    }

    @Override
    public RespBody page(UserDetailsImpl userDetails, TownPageDto townPageDto) {
        RespBody body = new RespBody();
        int begin = townPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, townPageDto.getSize(), Sort.Direction.fromString(townPageDto.getDirection()), townPageDto.getProperty());
        Page<Town> pageRes = townRepository.findAll((Specification<Town>)(root, query, cb) -> {
            Predicate predicate = root.isNotNull();
            predicate = cb.and(predicate, cb.equal(root.get("area").get("uid"), userDetails.getArea().getUid()));
            if (StringUtils.isNotEmpty(townPageDto.getSearchKey())){
                predicate = cb.and(predicate, cb.like(root.get("name").as(String.class), "%" + townPageDto.getSearchKey() + "%"));
            }
            return predicate;
        }, page);
        PageVo<TownPageVo> vo = new PageVo<>(pageRes, townPageDto);
        vo.setContent(pageRes.stream().map(TownPageVo::convert).collect(Collectors.toList()));
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody details(String uid) {
        RespBody body = new RespBody();
        Town town = townRepository.findByUid(uid);
        if (null == town){
            body.setMessage("找不到该镇");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        body.setData(TownDetailsVo.convert(town));
        return body;
    }

    @Override
    public RespBody add(UserDetailsImpl userDetails, TownAddDto townAddDto) {
        //添加镇需要创建镇管理员账号,设置相应的权限
        RespBody body = new RespBody();
        Town town = townRepository.findByAreaUidAndName(userDetails.getArea().getUid(), townAddDto.getName());
        if (null != town){
            body.setMessage("该镇已存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        town = townAddDto.convert();
        Area area = userDetails.getArea();
        town.setArea(area);
        LoginUP loginUP = new LoginUP();
        loginUP.setUsername(townAddDto.getAccount());
        loginUP.setPassword(townAddDto.getPassword());
        loginUP.setMobile(townAddDto.getMobile());
        Account account = new Account();
        account.setAccountRoles(Sets.newHashSet(accountRoleRepository.findByKeyword("BACKGROUND_ADMIN")));
        accountRepository.saveAndFlush(account);
        Voter voter = new Voter();
        voter.setAccount(account);
        voterRepository.saveAndFlush(voter);
        loginUP.setAccount(account);
        loginUPRepository.saveAndFlush(loginUP);
        townRepository.saveAndFlush(town);  //保存该镇
        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setTown(town);
        systemSetting.setLevel(LevelEnum.TOWN.getValue());
        systemSettingRepository.saveAndFlush(systemSetting);
        BackgroundAdmin backgroundAdmin = new BackgroundAdmin();
        backgroundAdmin.setAccount(account);
        backgroundAdmin.setTown(town);
        backgroundAdmin.setLevel(LevelEnum.TOWN.getValue());
        backgroundAdminRepository.saveAndFlush(backgroundAdmin);
        return body;
    }

    @Override
    public RespBody update(UserDetailsImpl userDetails, TownAddDto townAddDto) {
        RespBody body = new RespBody();
        Town town = townRepository.findByUid(townAddDto.getUid());
        if (town == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该镇");
            return body;
        }
        town.setName(townAddDto.getName());
        town.setDescription(townAddDto.getDescription());
        townRepository.saveAndFlush(town);
        return body;
    }

    @Override
    public RespBody delete(String uid) {
        RespBody body = new RespBody();
        Town town = townRepository.findByUid(uid);
        if (town == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该镇");
            return body;
        }
        if (town.getNpcMemberGroups().size() > 0 || town.getVillages().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前镇还包含代表小组/村的信息不能删除");
            return body;
        }
        townRepository.delete(town);
        return body;
    }
}
