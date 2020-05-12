package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.LevelDto;
import com.cdkhd.npc.entity.vo.CommentVo;
import com.cdkhd.npc.entity.vo.MemberUnitVo;
import com.cdkhd.npc.entity.vo.NpcMemberVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.NpcMemberRoleEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NpcMemberServiceImpl implements NpcMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberServiceImpl.class);

    private NpcMemberRepository npcMemberRepository;

    private TownRepository townRepository;

    private AreaRepository areaRepository;

    private NpcMemberGroupRepository npcMemberGroupRepository;

    private AccountRepository accountRepository;

    private SystemSettingRepository systemSettingRepository;

    private NpcMemberRoleRepository npcMemberRoleRepository;


    @Autowired
    public NpcMemberServiceImpl(NpcMemberRepository npcMemberRepository, TownRepository townRepository, AreaRepository areaRepository, NpcMemberGroupRepository npcMemberGroupRepository, AccountRepository accountRepository, SystemSettingRepository systemSettingRepository, NpcMemberRoleRepository npcMemberRoleRepository) {
        this.npcMemberRepository = npcMemberRepository;
        this.townRepository = townRepository;
        this.areaRepository = areaRepository;
        this.npcMemberGroupRepository = npcMemberGroupRepository;
        this.accountRepository = accountRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
    }

    /**
     * 获取镇、组的列表及下面的代表列表
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @Override
    public RespBody relationOfNpcMember(MobileUserDetailsImpl userDetails, LevelDto levelDto) {
        RespBody body = new RespBody();
        List<MemberUnitVo> MemberUnitVos = Lists.newArrayList();
        SystemSetting systemSetting;
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMemberRole specialMan = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.SPECIAL_MAN.getKeyword());
        if (levelDto.getLevel().equals(LevelEnum.TOWN.getValue())){
            //如果传了需要查询的镇的小组那么就按照产过来的查询，如果没有传过来，那么就按照当前登录人所在的镇来查询
            String townUid = userDetails.getTown().getUid();
            //开关判断能否给非本小组的代表提意见
            systemSetting  = systemSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(),townUid);
            //如果是镇上，就查询小组
            Town town = townRepository.findByUid(townUid);
            Set<NpcMemberGroup> groupList = town.getNpcMemberGroups();
            for (NpcMemberGroup npcMemberGroup : groupList) {//每个小组里面的代表信息
                if (systemSetting.getVoterOpinionToAll() || (!systemSetting.getVoterOpinionToAll() && account.getVoter().getVillage().getNpcMemberGroup().getUid().equals(npcMemberGroup.getUid()))){
                    MemberUnitVo memberUnitVo = MemberUnitVo.convert(npcMemberGroup.getUid(),npcMemberGroup.getName(),levelDto.getLevel(),npcMemberGroup.getCreateTime());
                    List<MemberUnitVo> members = npcMemberGroup.getMembers().stream()
                            .filter(member -> member.getStatus().equals(StatusEnum.ENABLED.getValue()) && !member.getIsDel() && !member.getType().equals(specialMan.getUid()) && (systemSetting.getVoterOpinionToAll() || (!systemSetting.getVoterOpinionToAll() && account.getVoter().getVillage().getNpcMemberGroup().getUid().equals(member.getNpcMemberGroup().getUid()))))
                            .map(member -> MemberUnitVo.convert(member.getUid(),member.getName(),levelDto.getLevel(),member.getCreateTime()))
                            .sorted(Comparator.comparing(MemberUnitVo::getCreateTime)).collect(Collectors.toList());
                    memberUnitVo.setChildren(members);
                    MemberUnitVos.add(memberUnitVo);
                }
            }
            MemberUnitVo memberUnitVo = MemberUnitVo.convert(townUid,"区代表",LevelEnum.AREA.getValue(),new Date());
            List<NpcMember> npcMembers = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(townUid,LevelEnum.AREA.getValue());
            memberUnitVo.setChildren(npcMembers.stream()
                    .filter(member -> member.getStatus().equals(StatusEnum.ENABLED.getValue()) && !member.getIsDel() && !member.getType().equals(specialMan.getUid()))
                    .map(member -> MemberUnitVo.convert(member.getUid(),member.getName(),LevelEnum.AREA.getValue(),member.getCreateTime()))
                    .sorted(Comparator.comparing(MemberUnitVo::getCreateTime)).collect(Collectors.toList()));
            MemberUnitVos.add(0,memberUnitVo);
        }else{
            String areaUid = userDetails.getArea().getUid();
            //开关判断能否给非本镇的代表提意见
            systemSetting  = systemSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(),areaUid);
            Area area = areaRepository.findByUid(areaUid);
            Set<Town> towns = area.getTowns();
            for (Town town : towns) {
                if (town.getStatus().equals(StatusEnum.ENABLED.getValue()) && !town.getIsDel()  && (systemSetting.getVoterOpinionToAll() || (!systemSetting.getVoterOpinionToAll() && account.getVoter().getTown().getUid().equals(town.getUid())))) {
                    MemberUnitVo memberUnitVo = MemberUnitVo.convert(town.getUid(), town.getName(), levelDto.getLevel(), town.getCreateTime());
                    List<MemberUnitVo> members = town.getNpcMembers().stream()
                            .filter(member -> member.getLevel().equals(LevelEnum.AREA.getValue()) && member.getStatus().equals(StatusEnum.ENABLED.getValue()) && !member.getIsDel() && !member.getType().equals(specialMan.getUid()) && (systemSetting.getVoterOpinionToAll() || (!systemSetting.getVoterOpinionToAll() && account.getVoter().getTown().getUid().equals(member.getTown().getUid()))))
                            .map(member -> MemberUnitVo.convert(member.getUid(), member.getName(), levelDto.getLevel(), member.getCreateTime()))
                            .sorted(Comparator.comparing(MemberUnitVo::getCreateTime)).collect(Collectors.toList());
                    memberUnitVo.setChildren(members);
                    MemberUnitVos.add(memberUnitVo);
                }
            }
        }
        MemberUnitVos.sort(Comparator.comparing(MemberUnitVo::getCreateTime));
        body.setData(MemberUnitVos);
        return body;
    }

    @Override
    public RespBody memberUnitDetails(LevelDto levelDto) {
        RespBody body = new RespBody();
        CommentVo commentVo = new CommentVo();
        if (levelDto.getLevel().equals(LevelEnum.TOWN.getValue())){
            NpcMemberGroup npcMemberGroup = npcMemberGroupRepository.findByUid(levelDto.getUid());
            if (npcMemberGroup != null) {
                commentVo = CommentVo.convert(npcMemberGroup.getUid(), npcMemberGroup.getName(), npcMemberGroup.getDescription());
            }
        }else if(levelDto.getLevel().equals(LevelEnum.AREA.getValue())){
            Town town = townRepository.findByUid(levelDto.getUid());
            if (town != null) {
                commentVo = CommentVo.convert(town.getUid(), town.getName(), town.getDescription());
            }
        }
        body.setData(commentVo);
        return body;
    }

    @Override
    public RespBody npcMemberDetails(BaseDto baseDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(baseDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该代表！");
            LOGGER.error("代表uid为空");
            return body;
        }
        NpcMember npcMember = npcMemberRepository.findByUid(baseDto.getUid());
        if (npcMember == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该代表！");
            LOGGER.error("代表查询出来为null");
            return body;
        }
        NpcMemberVo npcMemberVo = NpcMemberVo.convert(npcMember);
        body.setData(npcMemberVo);
        return body;
    }


    /**
     * 分页查询代表信息
     * @param level 查询条件
     * @param uid 查询条件
     * @return 查询结果
     */
    @Override
    public RespBody allNpcMembers(Byte level, String uid) {

        RespBody body = new RespBody();
        //其它查询条件
        NpcMemberRole specialMan = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.SPECIAL_MAN.getKeyword());
        Specification<NpcMember> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //查询与bgAdmin同级的代表
            predicateList.add(cb.equal(root.get("level"), level));
            predicateList.add(cb.isFalse(root.get("isDel")));
            predicateList.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            predicateList.add(cb.notEqual(root.get("type").as(String.class), specialMan.getUid()));
            //同镇的代表 or 同区的代表
            if (level.equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("npcMemberGroup").get("uid"), uid));
            } else if (level.equals(LevelEnum.AREA.getValue())) {
                predicateList.add(cb.equal(root.get("town").get("uid"), uid));
            }
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        List<NpcMember> npcMembers = npcMemberRepository.findAll(spec);
        //返回数据
        List<NpcMemberVo> npcMemberVos = npcMembers.stream().map(NpcMemberVo::convert).collect(Collectors.toList());
        body.setData(npcMemberVos);
        return body;
    }

    @Override
    public RespBody npcMemberUnits(MobileUserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = new RespBody();
        List<MemberUnitVo> memberUnitVos;
        Account account = accountRepository.findByUid(userDetails.getUid());
        if (level.equals(LevelEnum.TOWN.getValue())){
            //如果是镇上，就查询小组
            //如果传了需要查询的镇的小组那么就按照产过来的查询，如果没有传过来，那么就按照当前登录人所在的镇来查询
            String townUid;
            if (StringUtils.isEmpty(uid)){
                townUid = userDetails.getTown().getUid();
            }else{
                townUid = uid;
            }
            Town town = townRepository.findByUid(townUid);
            Set<NpcMemberGroup> groupList = town.getNpcMemberGroups();
            memberUnitVos = groupList.stream().map(group -> MemberUnitVo.convert(group.getUid(),group.getName(),level,group.getCreateTime(),account.getVoter().getVillage().getNpcMemberGroup().getUid().equals(group.getUid()))).sorted(Comparator.comparing(MemberUnitVo::getCreateTime)).collect(Collectors.toList());
            if (StringUtils.isEmpty(uid)){
                MemberUnitVo memberUnitVo = new MemberUnitVo();
                memberUnitVo.setLevel(LevelEnum.AREA.getValue());
                memberUnitVo.setIsCurrent(true);//区代表不受限制
                memberUnitVo.setName(town.getType().equals(LevelEnum.TOWN.getValue())?"区代表":"代表区上履职");
                memberUnitVo.setUid(townUid);
                memberUnitVos.add(0,memberUnitVo);
            }
        }else{
            String areaUid;
            if (StringUtils.isEmpty(uid)){
                areaUid = userDetails.getArea().getUid();
            }else{
                areaUid = uid;
            }
            Area area = areaRepository.findByUid(areaUid);
            Set<Town> towns = area.getTowns();
            memberUnitVos = towns.stream().filter(town -> town.getStatus().equals(StatusEnum.ENABLED.getValue()) && !town.getIsDel()).map(town -> MemberUnitVo.convert(town.getUid(),town.getName(),level,town.getCreateTime(),account.getVoter().getTown().getUid().equals(town.getUid()))).sorted(Comparator.comparing(MemberUnitVo::getCreateTime)).collect(Collectors.toList());
        }
        body.setData(memberUnitVos);
        return body;
    }

}
