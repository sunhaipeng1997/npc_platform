package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.entity.vo.NpcMemberVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.util.SysUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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
    private NpcMemberGroupRepository npcMemberGroupRepository;
    private TownRepository townRepository;

    @Autowired
    public NpcMemberServiceImpl(AccountRepository accountRepository, NpcMemberRepository npcMemberRepository, SessionRepository sessionRepository, NpcMemberGroupRepository npcMemberGroupRepository, TownRepository townRepository) {
        this.accountRepository = accountRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.sessionRepository = sessionRepository;
        this.npcMemberGroupRepository = npcMemberGroupRepository;
        this.townRepository = townRepository;
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
     * @param userDetails 当前用户
     * @param dto 待添加的代表信息
     * @return 添加结果
     */
    @Override
    public RespBody addNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        BackgroundAdmin bgAdmin = account.getBackgroundAdmin();

        //待新增代表的uid
        String memberUid = SysUtil.uid();
        //保存代表头像至文件系统
        String res = ImageUploadUtil.saveImage("npc_member_avatar", memberUid, dto.getAvatar());
        if (res.equals("error")) {
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            body.setMessage("图片保存失败！请稍后重试");
            LOGGER.error("代表头像保存失败, 新增代表失败");
            return body;
        }

        //设置代表信息
        NpcMember member = new NpcMember();
        BeanUtils.copyProperties(dto, member);
        member.setUid(memberUid);
        member.setAvatar(res);
        member.setLevel(bgAdmin.getLevel());
        member.setArea(bgAdmin.getArea());   //与后台管理员同区

        if (bgAdmin.getLevel().equals(LevelEnum.TOWN.getValue())) {
            member.setTown(bgAdmin.getTown());   //与后台管理员同镇

            NpcMemberGroup group = npcMemberGroupRepository.findByUid(dto.getWorkUnitUid());
            if (group == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("所选的小组不存在");
                LOGGER.error("uid为 {} 的小组不存在，添加代表失败", dto.getWorkUnitUid());
                return body;
            } else {
                member.setNpcMemberGroup(group);   //设置工作小组
            }
        } else if (bgAdmin.getLevel().equals(LevelEnum.AREA.getValue())) {
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

        //保存代表
        npcMemberRepository.save(member);

        body.setMessage("添加代表成功");
        return body;
    }

    /**
     * 修改代表信息
     * @param dto 待修改的代表信息
     * @return 修改结果
     */
    @Override
    public RespBody updateNpcMember(NpcMemberAddDto dto) {
        RespBody body = new RespBody();

        NpcMember member = npcMemberRepository.findByUid(dto.getUid());
        if (member == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("要修改的代表不存在");
            LOGGER.warn("uid为 {} 的代表不存在，修改代表信息失败", dto.getUid());
            return body;
        }

        if (dto.getAvatar() != null) {
            //保存代表头像至文件系统
            String res = ImageUploadUtil.saveImage("npc_member_avatar", dto.getUid(), dto.getAvatar());
            if (res.equals("error")) {
                body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                body.setMessage("图片保存失败！请稍后重试");
                LOGGER.error("代表头像保存失败，修改代表失败");
                return body;
            }
            member.setAvatar(res);
        }

        //拷贝要修改代的属性
        BeanUtils.copyProperties(dto, member);

        //保存代表
        npcMemberRepository.save(member);

        body.setMessage("修改代表信息成功");
        return body;
    }

    /**
     * 逻辑删除代表信息
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
        member.setIsDel((byte)1);
        npcMemberRepository.save(member);

        body.setMessage("删除代表成功");
        return body;
    }
}
