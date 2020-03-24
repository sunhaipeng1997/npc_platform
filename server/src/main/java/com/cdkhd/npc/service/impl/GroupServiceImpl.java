package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.NpcMemberGroup;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.entity.Village;
import com.cdkhd.npc.entity.dto.GroupAddDto;
import com.cdkhd.npc.entity.dto.GroupPageDto;
import com.cdkhd.npc.entity.vo.GroupDetailsVo;
import com.cdkhd.npc.entity.vo.GroupPageVo;
import com.cdkhd.npc.repository.base.NpcMemberGroupRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.VillageRepository;
import com.cdkhd.npc.service.GroupService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    private final NpcMemberGroupRepository npcMemberGroupRepository;

    private final TownRepository townRepository;

    private final VillageRepository villageRepository;

    @Autowired
    public GroupServiceImpl(NpcMemberGroupRepository npcMemberGroupRepository, TownRepository townRepository, VillageRepository villageRepository) {
        this.npcMemberGroupRepository = npcMemberGroupRepository;
        this.townRepository = townRepository;
        this.villageRepository = villageRepository;
    }

    @Override
    public RespBody page(UserDetailsImpl userDetails, GroupPageDto groupPageDto) {
        RespBody body = new RespBody();
        int begin = groupPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, groupPageDto.getSize(), Sort.Direction.fromString(groupPageDto.getDirection()), groupPageDto.getProperty());

        Page<NpcMemberGroup> pageRes = npcMemberGroupRepository.findAll((Specification<NpcMemberGroup>)(root, query, cb) -> {
            Predicate predicate = root.isNotNull();
            predicate = cb.and(predicate, cb.equal(root.get("town"), userDetails.getTown()));
            if (StringUtils.isNotEmpty(groupPageDto.getSearchKey())){
                predicate = cb.and(predicate, cb.like(root.get("name").as(String.class), "%" + groupPageDto.getSearchKey() + "%"));
            }
            return predicate;
        }, page);
        PageVo<GroupPageVo> vo = new PageVo<>(pageRes, groupPageDto);
//        LOGGER.info(pageRes.getContent().get(0).getTown().getName());
        vo.setContent(pageRes.stream().map(GroupPageVo::convert).collect(Collectors.toList()));
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody details(String uid) {
        RespBody body = new RespBody();
        NpcMemberGroup group = npcMemberGroupRepository.findByUid(uid);
        if (null == group){
            body.setMessage("找不到该小组");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        body.setData(GroupDetailsVo.convert(group));
        return body;
    }

    @Override
    public RespBody add(UserDetailsImpl userDetails, GroupAddDto groupAddDto) {
        RespBody body = new RespBody();
        NpcMemberGroup group = npcMemberGroupRepository.findByTownUidAndName(userDetails.getTown().getUid(), groupAddDto.getName());
        if (null != group){
            body.setMessage("小组名称已存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        group = groupAddDto.convert();
        Town town = townRepository.findByUid(userDetails.getTown().getUid());
        group.setTown(town);
        group.setArea(town.getArea());
        //保存该小组
        npcMemberGroupRepository.saveAndFlush(group);

        //修改包含的村
        Set<Village> villages = new HashSet<>();
        for (String uid : groupAddDto.getVillages()){
            Village village = villageRepository.findByUid(uid);
            village.setNpcMemberGroup(group);
            villages.add(village);
        }
        villageRepository.saveAll(villages);
        return body;
    }

    @Override
    public RespBody update(UserDetailsImpl userDetails, GroupAddDto groupAddDto) {
        RespBody body = new RespBody();
        NpcMemberGroup group = npcMemberGroupRepository.findByUid(groupAddDto.getUid());
        if (group == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该小组");
            return body;
        }
        group.setName(groupAddDto.getName());
        group.setDescription(groupAddDto.getDescription());
        //将包含的村重新关联
        Set<Village> villages = group.getVillages();
        for (Village village : villages){
            village.setNpcMemberGroup(null);
        }
        villageRepository.saveAll(villages);
        for (String uid : groupAddDto.getVillages()){
            Village village = villageRepository.findByUid(uid);
            village.setNpcMemberGroup(group);
            villages.add(village);
        }
        villageRepository.saveAll(villages);
        npcMemberGroupRepository.saveAndFlush(group);
        return body;
    }

    @Override
    public RespBody delete(String uid) {
        RespBody body = new RespBody();
        NpcMemberGroup group = npcMemberGroupRepository.findByUid(uid);
        if (group == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该小组");
            return body;
        }
        if (group.getVillages().size() > 0 || group.getMembers().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前小组还包含村/社区/代表的信息不能删除");
            return body;
        }
        npcMemberGroupRepository.delete(group);
        return body;
    }
}
