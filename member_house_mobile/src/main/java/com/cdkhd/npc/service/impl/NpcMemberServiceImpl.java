package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.NpcMemberVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NpcMemberServiceImpl implements NpcMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberServiceImpl.class);

    private NpcMemberRepository npcMemberRepository;

    private TownRepository townRepository;

    private AreaRepository areaRepository;

    @Autowired
    public NpcMemberServiceImpl(NpcMemberRepository npcMemberRepository, TownRepository townRepository, AreaRepository areaRepository) {
        this.npcMemberRepository = npcMemberRepository;
        this.townRepository = townRepository;
        this.areaRepository = areaRepository;
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
        Specification<NpcMember> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //查询与bgAdmin同级的代表
            predicateList.add(cb.equal(root.get("level"), level));
            predicateList.add(cb.isFalse(root.get("isDel")));
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
    public RespBody npcMemberUnits(UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = new RespBody();
        List<CommonVo> commonVos;
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
            commonVos = groupList.stream().map(group -> CommonVo.convert(group.getUid(),group.getName())).collect(Collectors.toList());
        }else{
            String areaUid;
            if (StringUtils.isEmpty(uid)){
                areaUid = userDetails.getArea().getUid();
            }else{
                areaUid = uid;
            }
            Area area = areaRepository.findByUid(areaUid);
            Set<Town> towns = area.getTowns();
            commonVos = towns.stream().map(town -> CommonVo.convert(town.getUid(),town.getName())).collect(Collectors.toList());
        }
        body.setData(commonVos);
        return body;
    }

}
