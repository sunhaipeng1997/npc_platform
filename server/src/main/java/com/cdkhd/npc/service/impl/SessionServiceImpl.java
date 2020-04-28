package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.SessionAddDto;
import com.cdkhd.npc.entity.dto.SessionPageDto;
import com.cdkhd.npc.entity.vo.SessionVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.AccountRoleRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SessionRepository;
import com.cdkhd.npc.service.SessionService;
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

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionServiceImpl.class);

    private SessionRepository sessionRepository;

    private NpcMemberRepository npcMemberRepository;

    private AccountRoleRepository accountRoleRepository;

    private AccountRepository accountRepository;

    @Autowired
    public SessionServiceImpl(SessionRepository sessionRepository, NpcMemberRepository npcMemberRepository, AccountRoleRepository accountRoleRepository, AccountRepository accountRepository) {
        this.sessionRepository = sessionRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * 获取届期列表
     *
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @Override
    public RespBody getSessions(UserDetailsImpl userDetails) {
        RespBody<List<CommonVo>> body = new RespBody<>();
        List<Session> sessions = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            sessions = sessionRepository.findByAreaUidAndLevel(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sessions = sessionRepository.findByTownUidAndLevel(userDetails.getTown().getUid(), LevelEnum.TOWN.getValue());
        }
        sessions.sort(Comparator.comparing(Session::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())));
        List<CommonVo> vos = sessions.stream().map(session ->
                CommonVo.convert(session.getUid(), session.getIsCurrent() ? session.getName() + "（本届）" : session.getName())).collect(Collectors.toList());
        body.setData(vos);
        return body;
    }

    @Override
    public RespBody sessionPage(UserDetailsImpl userDetails, SessionPageDto sessionPageDto) {
        RespBody body = new RespBody();
        //分页查询条件
        Pageable page = PageRequest.of(sessionPageDto.getPage() - 1, sessionPageDto.getSize(),
                Sort.Direction.fromString("ASC"), sessionPageDto.getProperty());

        //其它查询条件
        Page<Session> sessionPage = sessionRepository.findAll((Specification<Session>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNotNull(root.get("startDate").as(Date.class)));
            predicates.add(cb.isNotNull(root.get("endDate").as(Date.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //按名称查询
            if (StringUtils.isNotEmpty(sessionPageDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + sessionPageDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);

        PageVo<SessionVo> vo = new PageVo<>(sessionPage, sessionPageDto);
        List<SessionVo> sessionVos = sessionPage.getContent().stream().map(SessionVo::convert).collect(Collectors.toList());
        vo.setContent(sessionVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdateSession(UserDetailsImpl userDetails, SessionAddDto sessionAddDto) {
        RespBody body = new RespBody();
        if (sessionAddDto.getEndDate().before(sessionAddDto.getStartDate())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("结束日期不能在开始日期之前");
            LOGGER.warn("结束日期不能在开始日期之前");
            return body;
        }
        //验证传过来的日期与数据库中的日期是否有交叉
        Session session = new Session();
        Boolean result;
        List<Session> sessionList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(sessionAddDto.getUid())) {//修改届期
            session = sessionRepository.findByUid(sessionAddDto.getUid());
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {//镇代表
                sessionList = sessionRepository.findByTownUidAndLevelAndUidNotAndStartDateIsNotNull(userDetails.getTown().getUid(), userDetails.getLevel(), sessionAddDto.getUid());
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//区代表
                sessionList = sessionRepository.findByAreaUidAndLevelAndUidNotAndStartDateIsNotNull(userDetails.getArea().getUid(), userDetails.getLevel(), sessionAddDto.getUid());
            }
        } else {//添加届期
            session = new Session();
            session.setLevel(userDetails.getLevel());
            session.setArea(userDetails.getArea());
            session.setTown(userDetails.getTown());
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {//镇代表
                sessionList = sessionRepository.findByTownUidAndLevelAndStartDateIsNotNullAndEndDateIsNotNull(userDetails.getTown().getUid(), userDetails.getLevel());
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//区代表
                sessionList = sessionRepository.findByAreaUidAndLevelAndStartDateIsNotNullAndEndDateIsNotNull(userDetails.getArea().getUid(), userDetails.getLevel());
            }
        }
        result = this.validateSessionDate(sessionAddDto, sessionList);//验证届期之间的日期是否有交叉
        if (!result) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前时间段已有一届，请换时间段");
            LOGGER.warn("当前时间段已有一届，请换时间段");
            return body;
        } else {
            session.setName(sessionAddDto.getName());
            session.setStartDate(sessionAddDto.getStartDate());
            session.setEndDate(sessionAddDto.getEndDate());
            session.setRemark(sessionAddDto.getRemark());
            Boolean isCurrent = false;
            Date now = new Date();
            if (now.before(sessionAddDto.getEndDate()) && now.after(sessionAddDto.getStartDate())) {
                isCurrent = true;
            }
            session.setIsCurrent(isCurrent);
            sessionRepository.saveAndFlush(session);
        }
        return body;
    }

    private Boolean validateSessionDate(SessionAddDto sessionAddDto, List<Session> sessionList) {
        Boolean result = true;
        if (!CollectionUtils.isEmpty(sessionList)) {
            List<Date> startDate = Lists.newArrayList();
            List<Date> endDate = Lists.newArrayList();
            //先将传过来的日期加入集合
            startDate.add(sessionAddDto.getStartDate());
            endDate.add(sessionAddDto.getEndDate());
            //将原有的日期加入集合
            for (Session session : sessionList) {
                startDate.add(session.getStartDate());
                endDate.add(session.getEndDate());
            }
            //将起止日期分别进行排序
            startDate.sort(Comparator.naturalOrder());
            endDate.sort(Comparator.naturalOrder());
            for (int i = 1; i < startDate.size(); i++) {
                if (startDate.get(i).before(endDate.get(i - 1)) || startDate.equals(endDate)) {
                    //如果下一个起始日期小于于上一个结束日期，那么就有交叉
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public RespBody deleteSessions(UserDetailsImpl userDetails, String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到当前届次");
            LOGGER.warn("找不到当前届次");
            return body;
        }
        Session session = sessionRepository.findByUid(uid);
        if (session == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到当前届次");
            LOGGER.warn("找不到当前届次");
            return body;
        }
        //将删除的届期下的代表
        Set<NpcMember> members = session.getNpcMembers();
        if (CollectionUtils.isNotEmpty(members)) {
            //如果代表不为空，那么查询出默认届期
            Session defaultSession = this.defaultSession(userDetails);
            //找出这些代表中，不在本届的代表
            Session currentSession = this.currentSession(userDetails);
            if (!uid.equals(currentSession.getUid())) {//如果删除的不是本届
                List<NpcMember> npcMembers = Lists.newArrayList();//找出其中的本届代表
                for (NpcMember member : members) {
                    for (Session memberSession : member.getSessions()) {
                        if (currentSession.getUid().equals(memberSession.getUid())) {
                            npcMembers.add(member);
                            break;
                        }
                    }
                }
                members.removeAll(npcMembers);//本届代表不做处理，将其他代表一道默认届期中去
            } else {
                for (NpcMember member : members) {
                    //todo 清除掉这些代表的代表权限
                }
            }
            for (NpcMember member : members) {
                member.getSessions().removeIf(x -> x.getUid().equals(uid));//将代表要删除的届期移除掉
                member.getSessions().add(defaultSession);//添加上默认的届期
            }
        }
        sessionRepository.delete(session);
        return body;
    }

    @Override
    public RespBody clearSessions(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        //当前届期
        Session currentSession = this.currentSession(userDetails);
        if (null == currentSession) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前日期没有届次，不能换届！");
            LOGGER.warn("当前日期没有届次，不能换届！");
            return body;
        }
        List<NpcMember> allMembers = Lists.newArrayList();//查询相应的代表
        //区上点击换届，所有的区代表和街道代表都需要重新整理权限
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            //所有的区代表
            allMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(), userDetails.getLevel());
            List<NpcMember> streetMember = npcMemberRepository.findByTownType(LevelEnum.AREA.getValue());//查询所有的街道代表
            allMembers.addAll(streetMember);
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            allMembers = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(userDetails.getTown().getUid(), userDetails.getLevel());
        }
        List<NpcMember> currentMembers = Lists.newArrayList();//暂存本届代表
        for (NpcMember allMember : allMembers) {
            for (Session session : allMember.getSessions()) {
                if (session.getUid().equals(currentSession.getUid()) && allMember.getStatus().equals(StatusEnum.ENABLED.getValue())) {
                    currentMembers.add(allMember);
                }
            }
        }
        //将应该在本届的所有赋上应有的权限
        if (CollectionUtils.isNotEmpty(currentMembers)) {
            AccountRole memberRole = accountRoleRepository.findByKeyword(AccountRoleEnum.NPC_MEMBER.getKeyword());//将选民和代表身份都移除后加上选民身份
            for (NpcMember currentMember : currentMembers) {
                List<Account> accounts = accountRepository.findByMobile(currentMember.getMobile());//根据代表的手机号，去查询是否有注册账号
                //遍历账号，找到非后台管理员账号
                for (Account account : accounts) {
                    List<String> accountRole = account.getAccountRoles().stream().filter(role -> role.getStatus().equals(StatusEnum.ENABLED.getValue())).map(AccountRole::getKeyword).collect(Collectors.toList());
                    if (accountRole.contains(AccountRoleEnum.VOTER.getKeyword()) || accountRole.contains(AccountRoleEnum.NPC_MEMBER.getKeyword())){
                        //这个账号的角色包含了选民或者代表
                        Set<AccountRole> accountRoles = account.getAccountRoles();
                        accountRoles.removeIf(role -> role.getKeyword().equals(AccountRoleEnum.VOTER.getKeyword()));//将账号的选民身份删除
                        accountRoles.add(memberRole);//重新添加为代表身份
                        accountRoleRepository.saveAll(accountRoles);
                        currentMember.setAccount(account);//将账号和代表身份关联
                        break;
                    }
                }
            }
        }


        //过滤掉本届代表后剩下的非本届代表
        allMembers.removeAll(currentMembers);
        //清除剩下代表的权限
        Set<NpcMemberRole> npcMemberRoles;
        AccountRole voter = accountRoleRepository.findByKeyword(AccountRoleEnum.VOTER.getKeyword());//将选民和代表身份都移除后加上选民身份
        for (NpcMember allMember : allMembers) {
            //todo 清除权限
            npcMemberRoles = CollectionUtils.isEmpty(allMember.getNpcMemberRoles())? Sets.newHashSet():allMember.getNpcMemberRoles();
            npcMemberRoles.removeIf(role -> !role.getIsMust());//先把非必选的角色删除掉，只留下基本角色
            allMember.setNpcMemberRoles(npcMemberRoles);
            Account account = allMember.getAccount();
            //如果关联有小程序
            if (null != account){
                Set<AccountRole> accountRoles = account.getAccountRoles();
                accountRoles.removeIf(role -> role.getKeyword().equals(AccountRoleEnum.NPC_MEMBER.getKeyword()));//将账号的代表身份删除
                accountRoles.add(voter);//重新添加为选民身份
                accountRoleRepository.saveAll(accountRoles);
                allMember.setAccount(null);//将代表身份和小程序账号解绑
            }
        }
        npcMemberRepository.saveAll(allMembers);
        return body;
    }

    @Override
    public Session currentSession(UserDetailsImpl userDetails) {
        Session session = null;
        this.setCurrent(userDetails);//清除掉所有的本届
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            session = sessionRepository.findAreaCurrentSession(userDetails.getArea().getUid(), LevelEnum.AREA.getValue(), new Date());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            session = sessionRepository.findTownCurrentSession(userDetails.getTown().getUid(), LevelEnum.TOWN.getValue(), new Date());
        }
        if (session != null) {
            session.setIsCurrent(true);//把最新查询出来的设置为本届
            sessionRepository.saveAndFlush(session);
        }
        return session;
    }

    @Override
    public RespBody getCurrentSession(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        String currentUid = "";
        this.setCurrent(userDetails);//清除掉所有的本届
        Session session = null;
        this.setCurrent(userDetails);
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            session = sessionRepository.findAreaCurrentSession(userDetails.getArea().getUid(), LevelEnum.AREA.getValue(), new Date());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            session = sessionRepository.findTownCurrentSession(userDetails.getTown().getUid(), LevelEnum.TOWN.getValue(), new Date());
        }
        if (session != null) {
            currentUid = session.getUid();
            session.setIsCurrent(true);//把最新查询出来的设置为本届
            sessionRepository.saveAndFlush(session);
        }
        body.setData(currentUid);
        return body;
    }

    private void setCurrent(UserDetailsImpl userDetails){
        List<Session> sessions = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            sessions = sessionRepository.findByAreaUidAndLevel(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
            for (Session session : sessions) {
                session.setIsCurrent(false);
            }
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sessions = sessionRepository.findByTownUidAndLevel(userDetails.getTown().getUid(), LevelEnum.TOWN.getValue());
            for (Session session : sessions) {
                session.setIsCurrent(false);
            }
        }
        sessionRepository.saveAll(sessions);
    }

    @Override
    public Session defaultSession(UserDetailsImpl userDetails) {
        Session session = null;
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            session = sessionRepository.findByAreaUidAndLevelAndStartDateIsNullAndEndDateIsNull(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            session = sessionRepository.findByTownUidAndLevelAndStartDateIsNullAndEndDateIsNull(userDetails.getTown().getUid(), LevelEnum.TOWN.getValue());
        }
        return session;
    }

}
