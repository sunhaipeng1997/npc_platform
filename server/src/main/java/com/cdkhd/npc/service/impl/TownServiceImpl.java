package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.TownAddDto;
import com.cdkhd.npc.entity.dto.TownPageDto;
import com.cdkhd.npc.entity.vo.TownDetailsVo;
import com.cdkhd.npc.entity.vo.TownPageVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.TownService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Sets;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TownServiceImpl implements TownService {

    private final TownRepository townRepository;

    private final AccountRepository accountRepository;

    private final LoginUPRepository loginUPRepository;

    private final AccountRoleRepository accountRoleRepository;

    private final VoterRepository voterRepository;

    private final SystemSettingRepository systemSettingRepository;

    private final BackgroundAdminRepository backgroundAdminRepository;

    private final SessionRepository sessionRepository;

    private final SystemRepository systemRepository;

    private final PerformanceTypeRepository performanceTypeRepository;

    private Environment env;

    private PasswordEncoder passwordEncoder;


    @Autowired
    public TownServiceImpl(TownRepository townRepository, AccountRepository accountRepository, LoginUPRepository loginUPRepository, AccountRoleRepository accountRoleRepository, VoterRepository voterRepository, SystemSettingRepository systemSettingRepository, BackgroundAdminRepository backgroundAdminRepository, SessionRepository sessionRepository, SystemRepository systemRepository, PerformanceTypeRepository performanceTypeRepository, Environment env, PasswordEncoder passwordEncoder) {
        this.townRepository = townRepository;
        this.accountRepository = accountRepository;
        this.loginUPRepository = loginUPRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.voterRepository = voterRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.backgroundAdminRepository = backgroundAdminRepository;
        this.sessionRepository = sessionRepository;
        this.systemRepository = systemRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.env = env;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RespBody page(UserDetailsImpl userDetails, TownPageDto townPageDto) {
        RespBody body = new RespBody();
        int begin = townPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, townPageDto.getSize(), Sort.Direction.fromString(townPageDto.getDirection()), townPageDto.getProperty());
        Page<Town> pageRes = townRepository.findAll((Specification<Town>)(root, query, cb) -> {
            Predicate predicate = cb.isFalse(root.get("isDel").as(Boolean.class));
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
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        body.setData(TownDetailsVo.convert(town));
        return body;
    }

    @Override
    public RespBody add(UserDetailsImpl userDetails, TownAddDto townAddDto) {
        //???????????????????????????????????????,?????????????????????
        RespBody body = new RespBody();
        Town town = townRepository.findByAreaUidAndNameAndIsDelFalse(userDetails.getArea().getUid(), townAddDto.getName());
        if (null != town){
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (StringUtils.isEmpty(townAddDto.getAccount())){
            body.setMessage("????????????????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Account account = accountRepository.findByUsername(townAddDto.getAccount());
        if (account != null){
            body.setMessage("???????????????????????????????????????????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        town = townAddDto.convert();
        Area area = userDetails.getArea();
        town.setType(townAddDto.getType());
        town.setArea(area);
        town.setStatus(StatusEnum.ENABLED.getValue());
        townRepository.saveAndFlush(town);  //????????????

        if (townAddDto.getType().equals(LevelEnum.TOWN.getValue())){
            Session session = new Session();
            session.setName("??????");
            session.setLevel((byte)1);
            session.setTown(town);
            session.setRemark("????????????");
            sessionRepository.saveAndFlush(session);
        }

        final String accountSuffix = env.getProperty("account.suffix");
        final String accountPassword = env.getProperty("account.password");
        final String accountMobile = env.getProperty("account.mobile");

        LoginUP loginUP = new LoginUP();
        loginUP.setUsername(townAddDto.getAccount());
        loginUP.setPassword(passwordEncoder.encode(townAddDto.getPassword()));
//        loginUP.setMobile(townAddDto.getMobile());

        //??????????????????????????????
        LoginUP khd_loginUP = new LoginUP();
        khd_loginUP.setUsername(townAddDto.getAccount() + accountSuffix);
        khd_loginUP.setPassword(passwordEncoder.encode(accountPassword));
//        khd_loginUP.setMobile(accountMobile);

        account = new Account();
        account.setAccountRoles(Sets.newHashSet(accountRoleRepository.findByKeyword("BACKGROUND_ADMIN")));
        account.setStatus(StatusEnum.ENABLED.getValue());
        account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
        account.setMobile(townAddDto.getMobile());
        account.setIsDel(false);
        account.setLoginTimes(0);
        account.setUsername(townAddDto.getAccount());
        account.setSystems(systemRepository.findByKeyword(SystemEnum.MEMBER_HOUSE.toString()));
        accountRepository.saveAndFlush(account);

        //?????????
        Account khd_account = new Account();
        khd_account.setAccountRoles(Sets.newHashSet(accountRoleRepository.findByKeyword(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword())));
        khd_account.setStatus(StatusEnum.ENABLED.getValue());
        khd_account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
        khd_account.setMobile(accountMobile);
        khd_account.setIsDel(false);
        khd_account.setLoginTimes(0);
        khd_account.setUsername(townAddDto.getAccount() + accountSuffix);
        khd_account.setSystems(systemRepository.findByKeyword(SystemEnum.MEMBER_HOUSE.toString()));
        accountRepository.saveAndFlush(khd_account);

        loginUP.setAccount(account);
        loginUPRepository.saveAndFlush(loginUP);

        //?????????
        khd_loginUP.setAccount(khd_account);
        loginUPRepository.saveAndFlush(khd_loginUP);

        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setArea(area);
        systemSetting.setTown(town);
        systemSetting.setLevel(LevelEnum.TOWN.getValue());
        systemSettingRepository.saveAndFlush(systemSetting);

        BackgroundAdmin backgroundAdmin = new BackgroundAdmin();
        backgroundAdmin.setAccount(account);
        backgroundAdmin.setTown(town);
        backgroundAdmin.setArea(area);
        backgroundAdmin.setLevel(LevelEnum.TOWN.getValue());
        backgroundAdminRepository.saveAndFlush(backgroundAdmin);

        //?????????
        BackgroundAdmin khd_backgroundAdmin = new BackgroundAdmin();
        khd_backgroundAdmin.setAccount(khd_account);
        khd_backgroundAdmin.setTown(town);
        khd_backgroundAdmin.setArea(area);
        khd_backgroundAdmin.setLevel(LevelEnum.TOWN.getValue());
        backgroundAdminRepository.saveAndFlush(khd_backgroundAdmin);

        int temp = 1;
        //????????????????????????????????????????????? ??????????????????????????????????????????
        if (townAddDto.getType().equals(LevelEnum.TOWN.getValue())){
            for (PerformanceTypeEnum performanceTypeEnum : PerformanceTypeEnum.values()) {
                PerformanceType performanceType = performanceTypeRepository.findByNameAndLevelAndTownUidAndStatusAndIsDelFalse(performanceTypeEnum.getValue(), LevelEnum.TOWN.getValue(),town.getUid(), StatusEnum.ENABLED.getValue());
                if (performanceType == null) {
//                Integer maxSequence = performanceTypeRepository.findMaxSequenceByLevelAndAreaUid(LevelEnum.AREA.getValue(), area.getUid());
                    performanceType = new PerformanceType();
                    performanceType.setSequence(temp++);
                    performanceType.setName(performanceTypeEnum.getValue());
                    performanceType.setRemark("??????????????????????????????");
                    performanceType.setTown(town);
                    performanceType.setArea(userDetails.getArea());
                    performanceType.setLevel(LevelEnum.TOWN.getValue());
                    performanceType.setIsDel(false);
                    performanceType.setStatus(StatusEnum.ENABLED.getValue());
                    performanceType.setIsDefault(true);
                    performanceTypeRepository.saveAndFlush(performanceType);
                }
            }
        }
        return body;
    }

    @Override
    public RespBody update(UserDetailsImpl userDetails, TownAddDto townAddDto) {
        RespBody body = new RespBody();
        Town town = townRepository.findByUid(townAddDto.getUid());
        if (town == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????");
            return body;
        }
        Town town1 = townRepository.findByAreaUidAndNameAndUidIsNot(userDetails.getArea().getUid(), townAddDto.getName(), town.getUid());
        if (town1 != null){
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
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
            body.setMessage("???????????????");
            return body;
        }
        if (town.getNpcMemberGroups().size() > 0 || town.getVillages().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("??????????????????????????????/????????????????????????");
            return body;
        }
        if (town.getNpcMembers().size() > 0 || town.getVoters().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????/????????????????????????");
            return body;
        }
        town.setIsDel(true);
        townRepository.saveAndFlush(town);
        return body;
    }

    @Override
    public RespBody subTownsList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<Town> list = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            list = townRepository.findByAreaUidAndStatusAndIsDelFalse(userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = list.stream().map(town -> CommonVo.convert(town.getUid(), town.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }
}
