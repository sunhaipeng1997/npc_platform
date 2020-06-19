package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.UnitAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitPageDto;
import com.cdkhd.npc.entity.dto.UnitUserAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitUserPageDto;
import com.cdkhd.npc.entity.vo.UnitUserVo;
import com.cdkhd.npc.entity.vo.UnitVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitUserRepository;
import com.cdkhd.npc.service.UnitService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.collections.CollectionUtils;
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

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UnitServiceImpl implements UnitService {

    private PasswordEncoder passwordEncoder;

    private UnitRepository unitRepository;

    private UnitUserRepository unitUserRepository;

    private AccountRepository accountRepository;

    private AccountRoleRepository accountRoleRepository;

    private SuggestionBusinessRepository suggestionBusinessRepository;

    private Environment env;

    private LoginUPRepository loginUPRepository;

    private GovernmentUserRepository governmentUserRepository;

    private NpcMemberRepository npcMemberRepository;


    @Autowired
    public UnitServiceImpl(PasswordEncoder passwordEncoder, UnitRepository unitRepository, UnitUserRepository unitUserRepository, AccountRepository accountRepository, AccountRoleRepository accountRoleRepository, SuggestionBusinessRepository suggestionBusinessRepository, Environment env, LoginUPRepository loginUPRepository, GovernmentUserRepository governmentUserRepository, NpcMemberRepository npcMemberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.unitRepository = unitRepository;
        this.unitUserRepository = unitUserRepository;
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.env = env;
        this.loginUPRepository = loginUPRepository;
        this.npcMemberRepository = npcMemberRepository;
    }


    @Override
    public RespBody unitPage(UserDetailsImpl userDetails, UnitPageDto unitPageDto) {
        RespBody body = new RespBody();
        int begin = unitPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, unitPageDto.getSize(), Sort.Direction.fromString(unitPageDto.getDirection()), unitPageDto.getProperty());
        Page<Unit> unitPage = unitRepository.findAll((Specification<Unit>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            if (unitPageDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), unitPageDto.getStatus()));
            }
            //按名称查询
            if (StringUtils.isNotEmpty(unitPageDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + unitPageDto.getName() + "%"));
            }
            if (StringUtils.isNotEmpty(unitPageDto.getBusiness())){
                predicates.add(cb.equal(root.get("suggestionBusiness").get("uid").as(String.class), unitPageDto.getBusiness()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        PageVo<UnitVo> vo = new PageVo<>(unitPage, unitPageDto);
        List<UnitVo> unitVos = unitPage.getContent().stream().map(UnitVo::convert).collect(Collectors.toList());
        vo.setContent(unitVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdateUnit(UserDetailsImpl userDetails, UnitAddOrUpdateDto unitAddOrUpdateDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(unitAddOrUpdateDto.getName()) || StringUtils.isEmpty(unitAddOrUpdateDto.getMobile()) || StringUtils.isEmpty(unitAddOrUpdateDto.getBusiness())){
            body.setMessage("必填信息不能为空！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Unit unit = null;
        if (StringUtils.isNotEmpty(unitAddOrUpdateDto.getUid())){
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                unit = unitRepository.findByNameAndLevelAndAreaUidAndUidIsNotAndIsDelFalse(unitAddOrUpdateDto.getName(),LevelEnum.AREA.getValue(), userDetails.getArea().getUid(),unitAddOrUpdateDto.getUid());
            }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                unit = unitRepository.findByNameAndLevelAndTownUidAndUidIsNotAndIsDelFalse(unitAddOrUpdateDto.getName(),LevelEnum.TOWN.getValue(), userDetails.getTown().getUid(),unitAddOrUpdateDto.getUid());
            }
            if (unit != null){
                body.setMessage("单位信息已经存在！");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
            //修改
            unit = unitRepository.findByUid(unitAddOrUpdateDto.getUid());
            if (unit == null){
                body.setMessage("找不到单位信息！");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
        }else{
            //添加
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                unit = unitRepository.findByNameAndLevelAndAreaUidAndIsDelFalse(unitAddOrUpdateDto.getName(),LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
            }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                unit = unitRepository.findByNameAndLevelAndTownUidAndIsDelFalse(unitAddOrUpdateDto.getName(),LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
            }
            if (unit != null){
                body.setMessage("单位信息已经存在！");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
            unit = new Unit();
            unit.setLevel(userDetails.getLevel());
            unit.setArea(userDetails.getArea());
            unit.setTown(userDetails.getTown());
        }
        unit.setName(unitAddOrUpdateDto.getName());
        unit.setAddress(unitAddOrUpdateDto.getAddress());
        unit.setComment(unitAddOrUpdateDto.getComment());
        unit.setLatitude(unitAddOrUpdateDto.getLatitude());
        unit.setLongitude(unitAddOrUpdateDto.getLongitude());
        unit.setMobile(unitAddOrUpdateDto.getMobile());
        unit.setSuggestionBusiness(suggestionBusinessRepository.findByUid(unitAddOrUpdateDto.getBusiness()));
        unit.setAvatar(unitAddOrUpdateDto.getAvatar());
        unitRepository.saveAndFlush(unit);
        body.setData(unit.getUid());
        return body;
    }

    @Override
    public RespBody unitDetails(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setMessage("找不到单位信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Unit unit = unitRepository.findByUid(uid);
        if (unit ==null){
            body.setMessage("找不到单位信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        UnitVo unitVo = UnitVo.convert(unit);
        body.setData(unitVo);
        return body;
    }

    @Override
    public RespBody deleteUnit(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setMessage("找不到单位信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Unit unit = unitRepository.findByUid(uid);
        if (unit ==null){
            body.setMessage("找不到单位信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        unit.setIsDel(true);
        unitRepository.saveAndFlush(unit);
        return body;
    }

    @Override
    public RespBody changeUnitStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setMessage("找不到单位信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Unit unit = unitRepository.findByUid(uid);
        if (unit ==null){
            body.setMessage("找不到单位信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        unit.setStatus(status);
        unitRepository.saveAndFlush(unit);
        return body;
    }

    @Override
    public RespBody unitList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<Unit> units = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            units = unitRepository.findByLevelAndAreaUidAndStatusAndIsDelFalse(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            units = unitRepository.findByLevelAndTownUidAndStatusAndIsDelFalse(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = units.stream().map(unit -> CommonVo.convert(unit.getUid(),unit.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody unitUserPage(UserDetailsImpl userDetails, UnitUserPageDto unitUserPageDto) {
        RespBody body = new RespBody();
        int begin = unitUserPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, unitUserPageDto.getSize(), Sort.Direction.fromString(unitUserPageDto.getDirection()), unitUserPageDto.getProperty());
        Page<UnitUser> unitUsers = unitUserRepository.findAll((Specification<UnitUser>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("unit").get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("unit").get("uid").as(String.class),unitUserPageDto.getUnit()));
            predicates.add(cb.equal(root.get("unit").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("unit").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //按名称查询
            if (StringUtils.isNotEmpty(unitUserPageDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + unitUserPageDto.getName() + "%"));
            }
            //按登录账号查询
            if (StringUtils.isNotEmpty(unitUserPageDto.getUsername())) {
                predicates.add(cb.like(root.get("account").get("username").as(String.class), "%" + unitUserPageDto.getUsername() + "%"));
            }
            //按手机号查询
            if (StringUtils.isNotEmpty(unitUserPageDto.getMobile())) {
                predicates.add(cb.like(root.get("mobile").as(String.class), "%" + unitUserPageDto.getMobile() + "%"));
            }
            //按性别查询
            if (unitUserPageDto.getGender() != null) {
                predicates.add(cb.equal(root.get("gender").as(Byte.class), unitUserPageDto.getGender() ));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        PageVo<UnitUserVo> vo = new PageVo<>(unitUsers, unitUserPageDto);
        List<UnitUserVo> unitUserVos = unitUsers.getContent().stream().map(UnitUserVo::convert).collect(Collectors.toList());
        vo.setContent(unitUserVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdateUnitUser(UserDetailsImpl userDetails, UnitUserAddOrUpdateDto unitUserAddOrUpdateDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(unitUserAddOrUpdateDto.getName()) || StringUtils.isEmpty(unitUserAddOrUpdateDto.getMobile()) || StringUtils.isEmpty(unitUserAddOrUpdateDto.getUsername())){
            body.setMessage("必填信息不能为空！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (StringUtils.isEmpty(unitUserAddOrUpdateDto.getUid()) && (StringUtils.isEmpty(unitUserAddOrUpdateDto.getPassword()) || StringUtils.isEmpty(unitUserAddOrUpdateDto.getConfirmPwd()))){
            body.setMessage("必填信息不能为空！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (StringUtils.isEmpty(unitUserAddOrUpdateDto.getUid()) && !unitUserAddOrUpdateDto.getPassword().equals(unitUserAddOrUpdateDto.getConfirmPwd())){
            body.setMessage("两次输入的密码不一致！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        GovernmentUser governmentUser = governmentUserRepository.findByAccountMobile(unitUserAddOrUpdateDto.getMobile());//已进是政府的手机号，就不能设置为单位的
        if (governmentUser != null){
            body.setMessage("政府人员不能设置为办理单位人员！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        List<NpcMember> npcMembers = npcMemberRepository.findByMobileAndIsDelFalse(unitUserAddOrUpdateDto.getMobile());//代表也不能加为单位人员
        if (CollectionUtils.isNotEmpty(npcMembers)){
            body.setMessage("代表不能设置为办理单位人员！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        UnitUser unitUser = null;
        if (StringUtils.isNotEmpty(unitUserAddOrUpdateDto.getUid())){//单位人员只允许在一个部门任职
            unitUser = unitUserRepository.findByMobileAndUidIsNotAndIsDelFalse(unitUserAddOrUpdateDto.getMobile(), unitUserAddOrUpdateDto.getUid());
            if (unitUser != null){
                body.setMessage("单位人员信息已经存在！");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
            //修改
            unitUser = unitUserRepository.findByUid(unitUserAddOrUpdateDto.getUid());
            if (unitUser == null){
                body.setMessage("找不到单位人员信息！");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
            //如果修改了手机号，那么，清除原来的手机号绑定的信息
            if (!unitUser.getMobile().equals(unitUserAddOrUpdateDto.getMobile())){
                List<Account> accounts = accountRepository.findByMobile(unitUserAddOrUpdateDto.getMobile());//新手机号能查询出来的所有账号
                Account account = null;//单位人员对应的账号信息
                for (Account account1 : accounts) {//判断账号的身份
                    List<String> keywords = account1.getAccountRoles().stream().map(AccountRole::getKeyword).collect(Collectors.toList());
                    if (keywords.contains(AccountRoleEnum.VOTER.getKeyword()) || keywords.contains(AccountRoleEnum.NPC_MEMBER.getKeyword())) {//这个账号是选民或者是代表
                        account = account1;//把这个账号跟单位人员身份关联起来
                    }
                }
                if (account != null){
                    Set<AccountRole> accountRoleSet = account.getAccountRoles();//新手机号对应的角色
                    AccountRole unitRole = accountRoleRepository.findByKeyword(AccountRoleEnum.UNIT.getKeyword());//单位人员权限
                    accountRoleSet.add(unitRole);
                    account.setAccountRoles(accountRoleSet);//保存新的权限
                }
                account = unitUser.getAccount();
                account.setMobile(unitUserAddOrUpdateDto.getMobile());
                accountRepository.saveAndFlush(account);
                //移除旧号码的单位人员信息
                List<Account> oldAccounts = accountRepository.findByMobile(unitUser.getMobile());//旧手机号能查询出来的所有账号
                for (Account account1 : oldAccounts) {//判断账号的身份
                    List<String> keywords = account1.getAccountRoles().stream().map(AccountRole::getKeyword).collect(Collectors.toList());
                    if (keywords.contains(AccountRoleEnum.UNIT.getKeyword())) {//找到单位身份的账号
                        Set<AccountRole> accountRoleSet = account1.getAccountRoles();//这个账号的所有角色
                        accountRoleSet.removeIf(role -> role.getKeyword().equals(AccountRoleEnum.UNIT.getKeyword()));//移除掉办理单位的角色
                    }
                }
                accountRepository.saveAll(oldAccounts);
            }
        }else{
            //添加,一个人员只允许在一个单位
            unitUser = unitUserRepository.findByMobileAndIsDelFalse(unitUserAddOrUpdateDto.getMobile());
            if (unitUser != null){
                body.setMessage("单位人员信息已经存在！");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
            if (!unitUserAddOrUpdateDto.getPassword().equals(unitUserAddOrUpdateDto.getConfirmPwd())){
                body.setMessage("两次输入的密码不一致！");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
            unitUser = new UnitUser();
            unitUser.setUnit(unitRepository.findByUid(unitUserAddOrUpdateDto.getUnit()));

            //添加信息的时候，先查询手机号有没有注册过小程序
            List<Account> accounts = accountRepository.findByMobile(unitUserAddOrUpdateDto.getMobile());//手机号能查询出来的所有账号
            Account account = null;//单位人员对应的账号信息
            for (Account account1 : accounts) {//判断账号的身份
                List<String> keywords = account1.getAccountRoles().stream().map(AccountRole::getKeyword).collect(Collectors.toList());
                if (keywords.contains(AccountRoleEnum.VOTER.getKeyword()) || keywords.contains(AccountRoleEnum.NPC_MEMBER.getKeyword())) {//这个账号是选民的
                    account = account1;//把这个账号跟代表身份关联起来
                }
            }

            //创建账号
            if (account == null) {
                account = new Account();
                account.setMobile(unitUserAddOrUpdateDto.getMobile());//电话号码
                account.setLoginTimes(0);
            }
            Set<AccountRole> accountRoles = account.getAccountRoles();
            accountRoles.add(accountRoleRepository.findByKeyword(AccountRoleEnum.UNIT.getKeyword()));
            account.setAccountRoles(accountRoles);//办理单位
            account.setStatus(StatusEnum.ENABLED.getValue());
            account.setLoginWay(LoginWayEnum.LOGIN_UP.getValue());
            account.setIsDel(false);
            account.setUsername(unitUserAddOrUpdateDto.getUsername());
            accountRepository.saveAndFlush(account);

            //登录信息
            LoginUP loginUP = new LoginUP();
            loginUP.setUsername(unitUserAddOrUpdateDto.getUsername());
            loginUP.setPassword(passwordEncoder.encode(unitUserAddOrUpdateDto.getPassword()));
            loginUP.setAccount(account);
            loginUPRepository.saveAndFlush(loginUP);
            unitUser.setAccount(account);
        }
        unitUser.setName(unitUserAddOrUpdateDto.getName());
        unitUser.setGender(unitUserAddOrUpdateDto.getGender());
        unitUser.setComment(unitUserAddOrUpdateDto.getComment());
        unitUser.setMobile(unitUserAddOrUpdateDto.getMobile());
        unitUser.setAvatar(unitUserAddOrUpdateDto.getAvatar());
        unitUserRepository.saveAndFlush(unitUser);
        return body;
    }

    @Override
    public RespBody unitUserDetails(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setMessage("找不到单位人员信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        UnitUser unitUser = unitUserRepository.findByUid(uid);
        if (unitUser ==null){
            body.setMessage("找不到单位人员信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        UnitUserVo unitUserVo = UnitUserVo.convert(unitUser);
        body.setData(unitUserVo);
        return body;
    }

    @Override
    public RespBody deleteUnitUser(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setMessage("找不到单位人员信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        UnitUser unitUser = unitUserRepository.findByUid(uid);
        if (unitUser ==null){
            body.setMessage("找不到单位人员信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        unitUser.setIsDel(true);
        unitUserRepository.saveAndFlush(unitUser);
        return body;
    }

    @Override
    public RespBody resetPwd(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setMessage("找不到单位人员信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        UnitUser unitUser = unitUserRepository.findByUid(uid);
        if (unitUser ==null){
            body.setMessage("找不到单位人员信息！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        LoginUP loginUP = unitUser.getAccount().getLoginUP();
        final String accountPassword = env.getProperty("account.password");
        loginUP.setPassword(passwordEncoder.encode(accountPassword));
        loginUPRepository.saveAndFlush(loginUP);
        return body;
    }

    @Override
    public RespBody unitListByType(BaseDto baseDto) {
        RespBody body = new RespBody();
        List<Unit> units = unitRepository.findBySuggestionBusinessUidAndStatusAndIsDelFalse(baseDto.getUid(),StatusEnum.ENABLED.getValue());
        List<CommonVo> commonVos = units.stream().map(unit -> CommonVo.convert(unit.getUid(),unit.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }
}
