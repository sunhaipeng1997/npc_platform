package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.BackgroundAdmin;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Session;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.entity.vo.NpcMemberVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SessionRepository;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NpcMemberServiceImpl implements NpcMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberServiceImpl.class);

    private AccountRepository accountRepository;
    private NpcMemberRepository npcMemberRepository;
    private SessionRepository sessionRepository;

    @Autowired
    public NpcMemberServiceImpl(AccountRepository accountRepository, NpcMemberRepository npcMemberRepository, SessionRepository sessionRepository) {
        this.accountRepository = accountRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * 分页查询代表信息
     * @param userDetails 当前用户
     * @param pageDto 查询条件
     * @return 查询结果
     */
    @Override
    public RespBody pageOfNpcMembers(UserDetailsImpl userDetails, NpcMemberPageDto pageDto) {
        Account account = accountRepository.findByUid(userDetails.getUid());
        BackgroundAdmin bgAdmin = account.getBackgroundAdmin();

        //分页查询条件
        Pageable pageable = PageRequest.of(pageDto.getPage() - 1, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()), pageDto.getProperty());

        //按届期查询
        Specification<NpcMember> sessionSpec = (root, query, cb) -> {
            if (pageDto.getSessionUid() != null) {
                Join<NpcMember, Session> npcMemberSessionJoin = root.join("sessions", JoinType.RIGHT);
                return cb.equal(npcMemberSessionJoin.get("Session.uid"), pageDto.getSessionUid());
            }
            return null;
        };

        //其它查询条件
        Specification<NpcMember> otherSpec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //查询与bgAdmin同级的代表
            predicateList.add(cb.equal(root.get("level"), bgAdmin.getLevel()));
            //同镇的代表 or 同区的代表
            if (bgAdmin.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("town").get("uid"), bgAdmin.getTown().getUid()));
            } else if (bgAdmin.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicateList.add(cb.equal(root.get("area").get("uid"), bgAdmin.getArea().getUid()));
            }

            //按姓名模糊查询
            if (StringUtils.isNotBlank(pageDto.getNameKey())) {
                predicateList.add(cb.like(root.get("name"), "%" + pageDto.getNameKey() + "%"));
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
            if (pageDto.getJobType() != null) {
                predicateList.add(cb.equal(root.get("type"), pageDto.getJobType()));
            }
            //按工作单位查询
            if (StringUtils.isNotBlank(pageDto.getWorkUnitUid())) {
                String workUnit = "";
                if (bgAdmin.getLevel().equals(LevelEnum.TOWN.getValue())) {
                    workUnit = "group";
                } else if (bgAdmin.getLevel().equals(LevelEnum.AREA.getValue())) {
                    workUnit = "town";
                }
                predicateList.add(cb.equal(root.get(workUnit).get("uid"), pageDto.getWorkUnitUid()));
            }

            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        Page<NpcMember> page = npcMemberRepository.findAll(otherSpec.and(sessionSpec), pageable);

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
     * @param userDetails 当前用户身份
     * @param dto 待添加的代表信息
     * @return 添加结果
     */
    @Override
    public RespBody addNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto) {

        return null;
    }

    /**
     * 修改代表信息
     * @param userDetails 当前用户身份
     * @param dto 待修改的代表信息
     * @return 修改结果
     */
    @Override
    public RespBody updateNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto) {
        return null;
    }
}
