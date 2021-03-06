package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.entity.vo.MemberListVo;
import com.cdkhd.npc.entity.vo.NpcMemberVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.repository.suggestion_deal.UnitUserRepository;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.service.SessionService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NpcMemberServiceImpl implements NpcMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberServiceImpl.class);

    private NpcMemberRepository npcMemberRepository;
    private SessionRepository sessionRepository;
    private SessionService sessionService;
    private NpcMemberGroupRepository npcMemberGroupRepository;
    private NpcMemberRoleRepository npcMemberRoleRepository;
    private TownRepository townRepository;
    private AccountRepository accountRepository;
    private AccountRoleRepository accountRoleRepository;
    private GovernmentUserRepository governmentUserRepository;
    private UnitUserRepository unitUserRepository;

    @Autowired
    public NpcMemberServiceImpl(NpcMemberRepository npcMemberRepository, SessionRepository sessionRepository, SessionService sessionService, NpcMemberGroupRepository npcMemberGroupRepository, NpcMemberRoleRepository npcMemberRoleRepository, TownRepository townRepository, AccountRepository accountRepository, AccountRoleRepository accountRoleRepository, GovernmentUserRepository governmentUserRepository, UnitUserRepository unitUserRepository) {
        this.npcMemberRepository = npcMemberRepository;
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
        this.npcMemberGroupRepository = npcMemberGroupRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.townRepository = townRepository;
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.unitUserRepository = unitUserRepository;
    }

    /**
     * 分页查询代表信息
     *
     * @param userDetails 当前用户
     * @param pageDto     查询条件
     * @return 查询结果
     */
    @Override
    public RespBody pageOfNpcMembers(UserDetailsImpl userDetails, NpcMemberPageDto pageDto) {
        //分页查询条件
        Pageable pageable = PageRequest.of(pageDto.getPage() - 1, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()), pageDto.getProperty());

        //其它查询条件
        Specification<NpcMember> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //查询与bgAdmin同级的代表
            predicateList.add(cb.equal(root.get("level"), userDetails.getLevel()));
            predicateList.add(cb.isFalse(root.get("isDel")));
            predicateList.add(cb.equal(root.get("area").get("uid"), userDetails.getArea().getUid()));
            //同镇的代表 or 同区的代表
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("town").get("uid"), userDetails.getTown().getUid()));
            }

            //按姓名模糊查询
            if (StringUtils.isNotBlank(pageDto.getName())) {
                predicateList.add(cb.like(root.get("name"), "%" + pageDto.getName() + "%"));
            }
            //按手机号码模糊查询
            if (StringUtils.isNotBlank(pageDto.getPhone())) {
                predicateList.add(cb.like(root.get("mobile"), "%" + pageDto.getPhone() + "%"));
            }
            //按出生日期查询
            if (pageDto.getStartAt() != null) {
                predicateList.add(cb.greaterThanOrEqualTo(root.get("birthday"), pageDto.getStartAt()));
            }
            if (pageDto.getEndAt() != null) {
                predicateList.add(cb.lessThanOrEqualTo(root.get("birthday"), pageDto.getEndAt()));
            }
            //按职务类型查询
            if (StringUtils.isNotEmpty(pageDto.getJobType())) {
                predicateList.add(cb.equal(root.get("type"), pageDto.getJobType()));
            }
            //按工作单位查询
            if (StringUtils.isNotEmpty(pageDto.getWorkUnitUid())) {
                String workUnit = "";
                if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                    workUnit = "npcMemberGroup";
                } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                    workUnit = "town";
                }
                predicateList.add(cb.equal(root.get(workUnit).get("uid"), pageDto.getWorkUnitUid()));
            }

            if (StringUtils.isNotEmpty(pageDto.getSessionUid())) {
                Session session = sessionRepository.findByUid(pageDto.getSessionUid());
                Set<NpcMember> members = session.getNpcMembers();
                List<String> memberIds = Lists.newArrayList();
                for (NpcMember npcMember : members) {
                    memberIds.add(npcMember.getUid());
                }
                if (CollectionUtils.isNotEmpty(memberIds)) {
                    CriteriaBuilder.In<Object> in = cb.in(root.get("uid"));
                    in.value(memberIds);
                    predicateList.add(in);
                } else {
                    predicateList.add(cb.isNull(root.get("uid")));//届期里面没有人就不应该查询出东西来
                }
            }
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        Page<NpcMember> page = npcMemberRepository.findAll(spec, pageable);

        //封装查询结果
        PageVo<NpcMemberVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(page.get().map(NpcMemberVo::convert).collect(Collectors.toList()));

        //返回数据
        RespBody<PageVo> body = new RespBody<>();
        body.setData(pageVo);
        return body;
    }

    /**
     * 添加代表
     *
     * @param userDetails 当前用户
     * @param dto         待添加的代表信息
     * @return 添加结果
     */
    @Override
    public RespBody addOrUpdateNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto) {
        RespBody body = new RespBody();
        NpcMember member;
        GovernmentUser governmentUser = governmentUserRepository.findByAccountMobile(dto.getMobile());//查询下这个手机号，是不是政府的
        if (governmentUser != null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("政府人员不能添加为代表。");
            LOGGER.warn("手机号为 {} 是政府人员，不能添加为代表信息", dto.getMobile());
            return body;
        }
        UnitUser unitUser = unitUserRepository.findByMobileAndIsDelFalse(dto.getMobile());
        if (unitUser != null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("单位人员不能添加为代表。");
            LOGGER.warn("手机号为 {} 是单位人员，不能添加为代表信息", dto.getMobile());
            return body;
        }
        if (StringUtils.isNotEmpty(dto.getUid())) {
            member = npcMemberRepository.findByLevelAndMobileAndUidIsNotAndIsDelFalse(userDetails.getLevel(), dto.getMobile(), dto.getUid());//这里只是过滤等级，没有过滤镇，意思是一个代表只能在一个镇任职，不能使多个镇的镇代表
            if (member != null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("该代表已经存在");
                LOGGER.warn("手机号为 {} 代表已经存在，修改代表信息失败", dto.getMobile());
                return body;
            }
            member = npcMemberRepository.findByUid(dto.getUid());
            if (member == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("要修改的代表不存在");
                LOGGER.warn("uid为 {} 的代表不存在，修改代表信息失败", dto.getUid());
                return body;
            }
            //姓名一致性判断
            List<NpcMember> members = npcMemberRepository.findByMobileAndUidIsNotAndIsDelFalse(dto.getMobile(),dto.getUid());//通过手机号查询这个代表在其他地方的任职
            Boolean canAdd = true;
            for (NpcMember npcMember : members) {
                if (!npcMember.getName().equals(dto.getName())){
                    canAdd = false;//本次传入的姓名与系统中不一致
                }
            }
            if (!canAdd) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("代表姓名在系统中不一致");
                LOGGER.warn("手机号为 {} 代表已经存在，与本次添加的代表 {} 姓名不符", dto.getMobile(), dto.getName());
                return body;
            }

            body.setMessage("修改代表成功");
        } else {
            member = npcMemberRepository.findByLevelAndMobileAndIsDelFalse(userDetails.getLevel(), dto.getMobile());//这里只是过滤等级，没有过滤镇，意思是一个代表只能在一个镇任职，不能使多个镇的镇代表
            if (member != null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("该代表已经存在");
                LOGGER.warn("手机号为 {} 代表已经存在，修改代表信息失败", dto.getMobile());
                return body;
            }
            //姓名一致性判断
            List<NpcMember> members = npcMemberRepository.findByMobileAndIsDelFalse(dto.getMobile());//通过手机号查询这个代表在其他地方的任职
            Boolean canAdd = true;
            for (NpcMember npcMember : members) {
                if (!npcMember.getName().equals(dto.getName())){
                    canAdd = false;//本次传入的姓名与系统中不一致
                }
            }
            if (!canAdd) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("代表姓名在系统中不一致");
                LOGGER.warn("手机号为 {} 代表已经存在，与本次添加的代表 {} 姓名不符", dto.getMobile(), dto.getName());
                return body;
            }
            member = new NpcMember();
            member.setLevel(userDetails.getLevel());
            member.setArea(userDetails.getArea());   //与后台管理员同区
            body.setMessage("添加代表成功");
        }
        //设置代表信息
        member.setName(dto.getName());
        member.setMobile(dto.getMobile());
        member.setEmail(dto.getEmail());
        member.setAddress(dto.getAddress());
        member.setBirthday(dto.getBirthday());
        member.setGender(dto.getGender());
        NpcMemberRole npcMemberRole = npcMemberRoleRepository.findByUid(dto.getType());
        member.setType(npcMemberRole.getUid());
        member.setTypeName(npcMemberRole.getName());
        member.setSpecial(npcMemberRole.getSpecial());
        member.setCode(dto.getCode());
        member.setIdcard(dto.getIdcard());
        member.setAvatar(dto.getAvatar());
        member.setIntroduction(dto.getIntroduction());
        member.setComment(dto.getComment());
        member.setNation(dto.getNation());
        member.setEducation(dto.getEducation());
        member.setPolitical(dto.getPolitical());
        Set<Session> sessions = sessionRepository.findByUidIn(dto.getSessionUids());
        member.setSessions(sessions);

        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            member.setTown(userDetails.getTown());   //与后台管理员同镇

            NpcMemberGroup group = npcMemberGroupRepository.findByUid(dto.getWorkUnitUid());
            if (group == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("所选的小组不存在");
                LOGGER.error("uid为 {} 的小组不存在，添加代表失败", dto.getWorkUnitUid());
                return body;
            } else {
                member.setNpcMemberGroup(group);   //设置工作小组
            }
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            Town town = townRepository.findByUid(dto.getWorkUnitUid());
            if (town == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("所选的镇不存在");
                LOGGER.error("uid为 {} 的镇不存在，添加代表失败", dto.getWorkUnitUid());
                return body;
            } else {
                member.setTown(town);   //设置工作镇
            }
        }

        //本届的特殊职能
        Session currentSession = sessionService.currentSession(userDetails);
        Session defauleSession = sessionService.defaultSession(userDetails);
        String currentSessionId = "";
        String defauleSessionId = "";
        if (currentSession != null) {
            currentSessionId = currentSession.getUid();
        }
        defauleSessionId = defauleSession.getUid();

        List<Account> accounts = accountRepository.findByMobile(dto.getMobile());//这个手机号能查询出来的所有账号
        Account account = null;//代表对应的账号信息
        for (Account account1 : accounts) {//判断账号的身份，将后台管理员给过滤掉
            List<String> keywords = account1.getAccountRoles().stream().map(AccountRole::getKeyword).collect(Collectors.toList());
            if (!keywords.contains(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword()) && !keywords.contains(AccountRoleEnum.GOVERNMENT.getKeyword()) && account1.getVoter() != null) {//账号没有包含后台管理员,也不是政府的,并且注册了小程序
                account = account1;//把这个账号跟代表身份关联起来
            }
        }
        if (dto.getSessionUids().contains(defauleSessionId)) {//如果选择了其他届期，就清除掉非必选的角色，然后将账号角色改为选民
            Set<NpcMemberRole> npcMemberRoles = CollectionUtils.isEmpty(member.getNpcMemberRoles()) ? Sets.newHashSet() : member.getNpcMemberRoles();
            npcMemberRoles.clear();//先把所有的角色删除掉，然后将本次选择的加上
            member.setNpcMemberRoles(npcMemberRoles);
            if (account != null) {
                if (account.getNpcMembers().size() == 1) {//把所有的代表身份都设置为其他后，在设置为选民
                    Set<AccountRole> accountRoles = account.getAccountRoles();
                    accountRoles.removeIf(role -> role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword()));
                    AccountRole voter = accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword());//将选民和代表身份都移除后加上选民身份
                    accountRoles.add(voter);
                    accountRoleRepository.saveAll(accountRoles);
                }
                member.setAccount(null);
            }
            member.setStatus(StatusEnum.DISABLED.getValue());
        } else if (dto.getSessionUids().contains(currentSessionId)) {//如果选择的届期里面包含了当前的届期，那么就给代表赋予当前代表的职能
            Set<NpcMemberRole> npcMemberRoles = CollectionUtils.isEmpty(member.getNpcMemberRoles()) ? Sets.newHashSet() : member.getNpcMemberRoles();
            npcMemberRoles.removeIf(role -> role.getIsMust());//先把必选的角色删除掉，然后将本次选择的加上
            npcMemberRoles.add(npcMemberRole);
            member.setNpcMemberRoles(npcMemberRoles);
            member.setStatus(StatusEnum.ENABLED.getValue());
            if (account != null) {
                Set<AccountRole> accountRoles = account.getAccountRoles();
                accountRoles.removeIf(role -> role.getKeyword().equals(AccountRoleEnum.VOTER.getKeyword()));
                AccountRole memberAccount = accountRoleRepository.findByKeyword(AccountRoleEnum.NPC_MEMBER.getKeyword());//将选民和代表身份都移除后加上代表身份
                accountRoles.add(memberAccount);
                accountRoleRepository.saveAll(accountRoles);
                account.getVoter().setRealname(dto.getName());//统一姓名
                member.setAccount(account);
            }
        } else {//既不包含其他，也不包含本届，一样的设置为选民
            if (account != null) {
                if (account.getNpcMembers().size() == 1) {//把所有的代表身份删除后，在设置为选民
                    Set<AccountRole> accountRoles = account.getAccountRoles();
                    accountRoles.removeIf(role -> role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword()));
                    AccountRole voter = accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword());//将选民和代表身份都移除后加上选民身份
                    accountRoles.add(voter);
                    accountRoleRepository.saveAll(accountRoles);
                }
                member.setAccount(null);
            }
            member.setStatus(StatusEnum.DISABLED.getValue());
        }

        //保存代表
        npcMemberRepository.save(member);
        return body;
    }

    /**
     * 逻辑删除代表信息
     *
     * @param uid 待删除的代表uid
     * @return 删除结果
     */
    @Override
    public RespBody deleteNpcMember(String uid) {
        RespBody body = new RespBody();
        NpcMember member = npcMemberRepository.findByUid(uid);
        if (member == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("要删除的代表不存在");
            LOGGER.warn("uid为 {} 的代表不存在，删除代表信息失败", uid);
            return body;
        }

        //删除代表
        member.setIsDel(true);
        Account account = member.getAccount();//对应的账号信息
        if (account != null){
            Set<AccountRole> accountRoles= account.getAccountRoles();
            accountRoles.removeIf(role -> role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword()));//删除代表身份
            AccountRole voter = accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword());//添加选民身份
            accountRoles.add(voter);
            account.setAccountRoles(accountRoles);
            accountRepository.saveAndFlush(account);
        }
        member.setAccount(null);
        member.setStatus(StatusEnum.DISABLED.getValue());
        npcMemberRepository.save(member);
        body.setMessage("删除代表成功");
        return body;
    }

    /**
     * 添加代表信息时上传头像
     *
     * @param userDetails 当前用户身份
     * @param avatar      头像图片
     * @return 上传结果，上传成功返回图片访问url
     */
    @Override
    public RespBody uploadAvatar(UserDetailsImpl userDetails, MultipartFile avatar) {
        RespBody<String> body = new RespBody<>();
        if (avatar == null) {
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            body.setMessage("图片上传失败！请稍后重试");
            LOGGER.error("代表头像保存失败");
            return body;
        }
        //保存代表头像至文件系统
        String url = ImageUploadUtil.saveImage("npc_member_avatar", avatar, 150, 200);
        if (url.equals("error")) {
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            body.setMessage("图片上传失败！请稍后重试");
            LOGGER.error("代表头像保存失败");
            return body;
        }
        body.setMessage("头像上传成功");
        body.setData(url);
        return body;
    }

    /**
     * 获取代表的工作单位列表（镇/小组）
     *
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @Override
    public RespBody getWorkUnits(UserDetailsImpl userDetails) {
        RespBody<List<CommonVo>> body = new RespBody<>();
        List<CommonVo> vos;
        //如果当前后台管理员是镇后台管理员，则查询该镇的所有小组
        //如果当前后台管理员是区后台管理员，则查询该区的所有镇
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            List<NpcMemberGroup> npcMemberGroups = Lists.newArrayList(userDetails.getTown().getNpcMemberGroups());
            npcMemberGroups.sort(Comparator.comparing(NpcMemberGroup::getCreateTime));
            vos = npcMemberGroups.stream().map(group ->
                    CommonVo.convert(group.getUid(), group.getName())).collect(Collectors.toList());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            List<Town> towns = Lists.newArrayList(userDetails.getArea().getTowns());
            towns.sort(Comparator.comparing(Town::getCreateTime));
            vos = towns.stream().filter(town -> town.getStatus().equals(StatusEnum.ENABLED.getValue()) && !town.getIsDel()).map(town ->
                    CommonVo.convert(town.getUid(), town.getName())).collect(Collectors.toList());
        } else {
            throw new RuntimeException("当前后台管理员level不合法");
        }
        body.setData(vos);
        return body;
    }

    @Override
    public RespBody npcMemberList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<MemberListVo> memberListVos = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            List<NpcMemberGroup> memberGroups = npcMemberGroupRepository.findByTownUid(userDetails.getTown().getUid());
            memberListVos = memberGroups.stream().map(group -> MemberListVo.convert(group.getUid(), group.getName(), group.getMembers(), userDetails.getLevel())).collect(Collectors.toList());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            Set<Town> towns = userDetails.getArea().getTowns();
            memberListVos = towns.stream().map(town -> MemberListVo.convert(town.getUid(), town.getName(), town.getNpcMembers(), userDetails.getLevel())).collect(Collectors.toList());
        }
        body.setData(memberListVos);
        return body;
    }

    @Override
    public RespBody npcMemberListByGroup(UserDetailsImpl userDetails, BaseDto baseDto) {
        RespBody body = new RespBody();
        Set<NpcMember> npcMemberList = Sets.newHashSet();
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            NpcMemberGroup npcMemberGroup = npcMemberGroupRepository.findByUid(baseDto.getUid());
            npcMemberList = npcMemberGroup.getMembers();
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            Town town = townRepository.findByUid(baseDto.getUid());
            npcMemberList = town.getNpcMembers();
        }
        List<CommonVo> commonVos = npcMemberList.stream().filter(member -> !member.getIsDel() && member.getStatus().equals(StatusEnum.ENABLED.getValue())).map(member -> CommonVo.convert(member.getUid(), member.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;

    }

}
