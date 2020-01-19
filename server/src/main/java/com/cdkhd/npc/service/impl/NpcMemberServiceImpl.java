package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.NpcMemberGroup;
import com.cdkhd.npc.entity.Session;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.entity.vo.NpcMemberVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NpcMemberServiceImpl implements NpcMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberServiceImpl.class);

    private NpcMemberRepository npcMemberRepository;
    private SessionRepository sessionRepository;
    private NpcMemberGroupRepository npcMemberGroupRepository;
    private TownRepository townRepository;

    @Autowired
    public NpcMemberServiceImpl(NpcMemberRepository npcMemberRepository, SessionRepository sessionRepository, NpcMemberGroupRepository npcMemberGroupRepository, TownRepository townRepository) {
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
        //分页查询条件
        Pageable pageable = PageRequest.of(pageDto.getPage() - 1, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()), pageDto.getProperty());

        //其它查询条件
        Specification<NpcMember> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //查询与bgAdmin同级的代表
            predicateList.add(cb.equal(root.get("level"), userDetails.getLevel()));
            predicateList.add(cb.isFalse(root.get("isDel")));
            //同镇的代表 or 同区的代表
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("town").get("uid"), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicateList.add(cb.equal(root.get("area").get("uid"), userDetails.getArea().getUid()));
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
            if (pageDto.getJobType() != null) {
                predicateList.add(cb.equal(root.get("type"), pageDto.getJobType()));
            }
            //按工作单位查询
            if (StringUtils.isNotBlank(pageDto.getWorkUnitUid())) {
                String workUnit = "";
                if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                    workUnit = "npcMemberGroup";
                } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                    workUnit = "town";
                }
                predicateList.add(cb.equal(root.get(workUnit).get("uid"), pageDto.getWorkUnitUid()));
            }

            if (StringUtils.isNotEmpty(pageDto.getSessionUid())){
                Session session = sessionRepository.findByUid(pageDto.getSessionUid());
                Set<NpcMember> members = session.getNpcMembers();
                List<String> memberIds = Lists.newArrayList();
                for (NpcMember npcMember : members) {
                    memberIds.add(npcMember.getUid());
                }
                CriteriaBuilder.In<Object> in = cb.in(root.get("uid"));
                in.value(memberIds);
                predicateList.add(in);
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
     * @param userDetails 当前用户
     * @param dto 待添加的代表信息
     * @return 添加结果
     */
    @Override
    public RespBody addOrUpdateNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto) {
        RespBody body = new RespBody();
        NpcMember member;
        if (StringUtils.isNotEmpty(dto.getUid())){
            member = npcMemberRepository.findByUid(dto.getUid());
            if (member == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("要修改的代表不存在");
                LOGGER.warn("uid为 {} 的代表不存在，修改代表信息失败", dto.getUid());
                return body;
            }
            body.setMessage("修改代表成功");
        }else {
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
        member.setType(dto.getType());
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
        //保存代表
        npcMemberRepository.save(member);

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
        member.setIsDel(true);
        npcMemberRepository.save(member);

        body.setMessage("删除代表成功");
        return body;
    }

    /**
     * 添加代表信息时上传头像
     * @param userDetails 当前用户身份
     * @param avatar 头像图片
     * @return 上传结果，上传成功返回图片访问url
     */
    @Override
    public RespBody uploadAvatar(UserDetailsImpl userDetails, MultipartFile avatar) {
        RespBody<String> body = new RespBody<>();
        //保存代表头像至文件系统
        String url = ImageUploadUtil.saveImage("npc_member_avatar", avatar,150,200);
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
            Set<NpcMemberGroup> npcMemberGroups = userDetails.getTown().getNpcMemberGroups();
            vos = npcMemberGroups.stream().map(group ->
                    CommonVo.convert(group.getUid(), group.getName())).collect(Collectors.toList());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            Set<Town> towns = userDetails.getArea().getTowns();
            vos = towns.stream().map(town ->
                    CommonVo.convert(town.getUid(), town.getName())).collect(Collectors.toList());
        } else {
            throw new RuntimeException("当前后台管理员level不合法");
        }
        body.setData(vos);
        return body;
    }

}
