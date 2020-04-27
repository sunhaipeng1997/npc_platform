package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Session;
import com.cdkhd.npc.entity.dto.SessionAddDto;
import com.cdkhd.npc.entity.dto.SessionPageDto;
import com.cdkhd.npc.entity.vo.SessionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SessionRepository;
import com.cdkhd.npc.service.SessionService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
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

    @Autowired
    public SessionServiceImpl(SessionRepository sessionRepository, NpcMemberRepository npcMemberRepository) {
        this.sessionRepository = sessionRepository;
        this.npcMemberRepository = npcMemberRepository;
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
        List<Session> sessions;
        //如果当前后台管理员是区后台管理员或者街道后台管理员
        sessions = sessionRepository.findByAreaUidAndLevel(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
        //如果当前后台管理员是镇后台管理员
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) &&
                userDetails.getTown().getType().equals(LevelEnum.TOWN.getValue())) {
            sessions = sessionRepository.findByTownUidAndLevel(userDetails.getTown().getUid(), userDetails.getLevel());
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
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            allMembers = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(userDetails.getTown().getUid(), userDetails.getLevel());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            allMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(), userDetails.getLevel());
        }
        List<NpcMember> currentMembers = Lists.newArrayList();//暂存本届代表
        for (NpcMember allMember : allMembers) {
            for (Session session : allMember.getSessions()) {
                if (session.getUid().equals(currentSession.getUid())) {
                    currentMembers.add(allMember);
                }
            }
        }
        allMembers.removeAll(currentMembers);//过滤掉本届代表
        //清除剩下代表的权限
        for (NpcMember allMember : allMembers) {
            //todo 清除权限
        }
        npcMemberRepository.saveAll(allMembers);
        return body;
    }

    @Override
    public Session currentSession(UserDetailsImpl userDetails) {
        Session session = null;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            session = sessionRepository.findTownCurrentSession(userDetails.getTown().getUid(), userDetails.getLevel(), new Date());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            session = sessionRepository.findAreaCurrentSession(userDetails.getArea().getUid(), userDetails.getLevel(), new Date());
        }
        return session;
    }

    @Override
    public RespBody getCurrentSession(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        String currentUid = "";
        Session session = null;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            session = sessionRepository.findTownCurrentSession(userDetails.getTown().getUid(), userDetails.getLevel(), new Date());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            session = sessionRepository.findAreaCurrentSession(userDetails.getArea().getUid(), userDetails.getLevel(), new Date());
        }
        if (session != null) {
            currentUid = session.getUid();
        }
        body.setData(currentUid);
        return body;
    }

    @Override
    public Session defaultSession(UserDetailsImpl userDetails) {
        Session session = null;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            session = sessionRepository.findByTownUidAndLevelAndStartDateIsNullAndEndDateIsNull(userDetails.getTown().getUid(), userDetails.getLevel());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            session = sessionRepository.findByAreaUidAndLevelAndStartDateIsNullAndEndDateIsNull(userDetails.getArea().getUid(), userDetails.getLevel());
        }
        return session;
    }

}
