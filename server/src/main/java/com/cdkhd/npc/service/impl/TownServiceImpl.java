package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.TownAddDto;
import com.cdkhd.npc.entity.dto.TownPageDto;
import com.cdkhd.npc.entity.vo.TownDetailsVo;
import com.cdkhd.npc.entity.vo.TownPageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.LoginWayEnum;
import com.cdkhd.npc.enums.PerformanceTypeEnum;
import com.cdkhd.npc.enums.StatusEnum;
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
        Town town = townRepository.findByAreaUidAndNameAndIsDelFalse(userDetails.getArea().getUid(), townAddDto.getName());
        if (null != town){
            body.setMessage("该镇已存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (StringUtils.isEmpty(townAddDto.getAccount())){
            body.setMessage("请输入管理员账号");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Account account = accountRepository.findByUsername(townAddDto.getAccount());
        if (account != null){
            body.setMessage("该账号已存在，请换个账号重新输入！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        town = townAddDto.convert();
        Area area = userDetails.getArea();
        town.setType(townAddDto.getType());
        town.setArea(area);
        town.setStatus(StatusEnum.ENABLED.getValue());
        townRepository.saveAndFlush(town);  //保存该镇

        Session session = new Session();
        session.setName("其它");
        session.setLevel((byte)1);
        session.setTown(town);
        session.setRemark("其他情况");
        sessionRepository.saveAndFlush(session);

        final String accountSuffix = env.getProperty("account.suffix");
        final String accountPassword = env.getProperty("account.password");
        final String accountMobile = env.getProperty("account.mobile");

        LoginUP loginUP = new LoginUP();
        loginUP.setUsername(townAddDto.getAccount());
        loginUP.setPassword(passwordEncoder.encode(townAddDto.getPassword()));
        loginUP.setMobile(townAddDto.getMobile());

        //镇管理员公司默认账号
        LoginUP khd_loginUP = new LoginUP();
        khd_loginUP.setUsername(townAddDto.getAccount() + accountSuffix);
        khd_loginUP.setPassword(passwordEncoder.encode(accountPassword));
        khd_loginUP.setMobile(accountMobile);

        account = new Account();
        account.setAccountRoles(Sets.newHashSet(accountRoleRepository.findByKeyword("BACKGROUND_ADMIN")));
        account.setStatus(StatusEnum.ENABLED.getValue());
        account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
        account.setMobile(townAddDto.getMobile());
        account.setIsDel(false);
        account.setLoginTimes(0);
        account.setUsername(townAddDto.getAccount());
        account.setSystems(systemRepository.findByKeyword("MEMBER_HOUSE"));
        accountRepository.saveAndFlush(account);

        //科鸿达
        Account khd_account = new Account();
        khd_account.setAccountRoles(Sets.newHashSet(accountRoleRepository.findByKeyword("BACKGROUND_ADMIN")));
        khd_account.setStatus(StatusEnum.ENABLED.getValue());
        khd_account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
        khd_account.setMobile(accountMobile);
        khd_account.setIsDel(false);
        khd_account.setLoginTimes(0);
        khd_account.setUsername(townAddDto.getAccount() + accountSuffix);
        khd_account.setSystems(systemRepository.findByKeyword("MEMBER_HOUSE"));
        accountRepository.saveAndFlush(khd_account);

//        Voter voter = new Voter();
//        voter.setAccount(account);
//        voter.setTown(town);
//        voterRepository.saveAndFlush(voter);

        //科鸿达
//        Voter khd_voter = new Voter();
//        khd_voter.setAccount(khd_account);
//        khd_voter.setTown(town);
//        voterRepository.saveAndFlush(khd_voter);

        loginUP.setAccount(account);
        loginUPRepository.saveAndFlush(loginUP);

        //科鸿达
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

        //科鸿达
        BackgroundAdmin khd_backgroundAdmin = new BackgroundAdmin();
        khd_backgroundAdmin.setAccount(khd_account);
        khd_backgroundAdmin.setTown(town);
        khd_backgroundAdmin.setArea(area);
        khd_backgroundAdmin.setLevel(LevelEnum.TOWN.getValue());
        backgroundAdminRepository.saveAndFlush(khd_backgroundAdmin);

        int temp = 1;
        //给该镇初始化两个默认的履职类型 类型是镇就初始化两个履职类型
        if (townAddDto.getType().equals(LevelEnum.TOWN.getValue())){
            for (PerformanceTypeEnum performanceTypeEnum : PerformanceTypeEnum.values()) {
                PerformanceType performanceType = performanceTypeRepository.findByNameAndLevelAndTownUidAndStatusAndIsDelFalse(performanceTypeEnum.getValue(),LevelEnum.TOWN.getValue(),town.getUid(),StatusEnum.ENABLED.getValue());
                if (performanceType == null) {
//                Integer maxSequence = performanceTypeRepository.findMaxSequenceByLevelAndAreaUid(LevelEnum.AREA.getValue(), area.getUid());
                    performanceType = new PerformanceType();
                    performanceType.setSequence(temp++);
                    performanceType.setName(performanceTypeEnum.getValue());
                    performanceType.setRemark("初始化数据，不可删除");
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
            body.setMessage("找不到该镇");
            return body;
        }
        Town town1 = townRepository.findByAreaUidAndNameAndUidIsNot(userDetails.getArea().getUid(), townAddDto.getName(), town.getUid());
        if (town1 != null){
            body.setMessage("该镇已存在");
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
            body.setMessage("找不到该镇");
            return body;
        }
        if (town.getNpcMemberGroups().size() > 0 || town.getVillages().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前镇还包含代表小组/村的信息不能删除");
            return body;
        }
        if (town.getNpcMembers().size() > 0 || town.getVoters().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前镇还包含代表/选民信息不能删除");
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
