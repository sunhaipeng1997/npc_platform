package com.cdkhd.npc.service.impl;
/*
 * @description:政府模块服务层实现类
 * @author:liyang
 * @create:2020-05-20
 */

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.GovAddDto;
import com.cdkhd.npc.entity.vo.GovDetailVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.LoginWayEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.suggestion_deal.GovernmentRepository;
import com.cdkhd.npc.repository.suggestion_deal.SuggestionSettingRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitUserRepository;
import com.cdkhd.npc.service.GovService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GovServiceImpl implements GovService {

    private GovernmentRepository governmentRepository;

    private GovernmentUserRepository governmentUserRepository;

    private AccountRepository accountRepository;

    private AccountRoleRepository accountRoleRepository;

    private SystemRepository systemRepository;

    private LoginUPRepository loginUPRepository;

    private PasswordEncoder passwordEncoder;

    private NpcMemberRepository npcMemberRepository;

    private UnitUserRepository unitUserRepository;

    private SuggestionSettingRepository suggestionSettingRepository;

    @Autowired
    public GovServiceImpl(GovernmentRepository governmentRepository, GovernmentUserRepository governmentUserRepository, AccountRepository accountRepository, AccountRoleRepository accountRoleRepository, SystemRepository systemRepository, LoginUPRepository loginUPRepository, PasswordEncoder passwordEncoder, NpcMemberRepository npcMemberRepository, UnitUserRepository unitUserRepository, SuggestionSettingRepository suggestionSettingRepository) {
        this.governmentRepository = governmentRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.systemRepository = systemRepository;
        this.loginUPRepository = loginUPRepository;
        this.passwordEncoder = passwordEncoder;
        this.npcMemberRepository = npcMemberRepository;
        this.unitUserRepository = unitUserRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
    }

    @Override
    public RespBody addGovernment(UserDetailsImpl userDetails, GovAddDto govAddDto) {
        RespBody body = new RespBody();
        Government government = governmentRepository.findByAreaUidAndName(userDetails.getArea().getUid(), govAddDto.getName());
        if (government != null) {
            body.setMessage("该名称已被使用，请重新输入名称");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Account account = accountRepository.findByUsername(govAddDto.getAccount());  //账号名称不能重复
        if (account != null) {
            body.setMessage("该账号已存在，请换个账号重新输入");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        List<NpcMember> members = npcMemberRepository.findByMobileAndIsDelFalse(govAddDto.getMobile());//手机号不能重复
        if (!members.isEmpty()) {
            body.setMessage("该手机号已被使用，请重新输入");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        UnitUser unitUser = unitUserRepository.findByMobileAndIsDelFalse(govAddDto.getMobile());
        if (unitUser != null) {
            body.setMessage("该手机号已被使用，请重新输入");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        //创建政府账号
        account = new Account();
        account.setAccountRoles(Sets.newHashSet(accountRoleRepository.findByKeyword("GOVERNMENT")));
        account.setStatus(StatusEnum.ENABLED.getValue());
        account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
        account.setMobile(govAddDto.getMobile());
        account.setIsDel(false);
        account.setLoginTimes(0);
        account.setUsername(govAddDto.getAccount());
        account.setSystems(systemRepository.findByKeyword("SUGGESTION"));
        accountRepository.saveAndFlush(account);

        //创建政府账号loginUp
        LoginUP loginUP = new LoginUP();
        loginUP.setUsername(govAddDto.getAccount());
        loginUP.setPassword(passwordEncoder.encode(govAddDto.getPassword()));
        loginUP.setAccount(account);
        loginUPRepository.saveAndFlush(loginUP);

        //创建government
        government = new Government();
        government.setName(govAddDto.getName());
        government.setArea(userDetails.getArea());
        government.setTown(userDetails.getTown());
        government.setLevel(userDetails.getLevel());
        government.setDescription(govAddDto.getDescription());
        government.setLatitude(govAddDto.getLatitude());
        government.setLongitude(govAddDto.getLongitude());
        government.setAddress(govAddDto.getAddress());
        governmentRepository.saveAndFlush(government);

        //创建government_user
        GovernmentUser governmentUser = new GovernmentUser();
        governmentUser.setArea(userDetails.getArea());
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            governmentUser.setTown(userDetails.getTown());
        }
        governmentUser.setLevel(userDetails.getLevel());
        governmentUser.setGovernment(government);
        governmentUser.setAccount(account);
        governmentUserRepository.saveAndFlush(governmentUser);

        //设置一个SuggestionSetting
        SuggestionSetting suggestionSetting;
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(),userDetails.getArea().getUid());
        }else {
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid());
        }
        if(suggestionSetting == null){
            suggestionSetting = new SuggestionSetting();
            suggestionSetting.setLevel(userDetails.getLevel());
            suggestionSetting.setTown(userDetails.getTown());
            suggestionSetting.setArea(userDetails.getArea());
            suggestionSettingRepository.saveAndFlush(suggestionSetting);
        }
        return body;
    }

    @Override
    public RespBody updateGovernment(GovAddDto govAddDto) {
        RespBody body = new RespBody();
        Government government = governmentRepository.findByUid(govAddDto.getUid());
        if (government == null) {
            body.setMessage("请选择要修改的政府");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        government.setName(govAddDto.getName());
        government.setDescription(govAddDto.getDescription());
        governmentRepository.saveAndFlush(government);

//        Account account = government.getGovernmentUser().getAccount();
//        if (accountRepository.findByUsernameAndUidIsNot(govAddDto.getAccount(), account.getUid()) != null) {
//            body.setMessage("该账号已存在，请重新输入");
//            body.setStatus(HttpStatus.BAD_REQUEST);
//            return body;
//        }
//        account.setUsername(govAddDto.getAccount());
//        accountRepository.saveAndFlush(account);
//
//        LoginUP loginUP = account.getLoginUP();
//        loginUP.setUsername(govAddDto.getAccount());
//        loginUPRepository.saveAndFlush(loginUP);
        return body;
    }

    @Override
    public RespBody detailGovernment(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Government government;
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            //区人大后台管理员
            government = governmentRepository.findByAreaUidAndLevel(userDetails.getArea().getUid(), userDetails.getLevel());
        }else {
            //镇人大后台管理员
            government = governmentRepository.findByTownUid(userDetails.getTown().getUid());
        }
        if (government == null){
            body.setMessage("请添加政府");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        GovDetailVo govDetailVo = GovDetailVo.convert(government);
        govDetailVo.setAccount(government.getGovernmentUser().getAccount().getUsername());
        govDetailVo.setMobile(government.getGovernmentUser().getAccount().getMobile());
        body.setData(govDetailVo);
        return body;
    }
}
