package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.entity.vo.EdgeVo;
import com.cdkhd.npc.entity.vo.NodeVo;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AreaRepository;
import com.cdkhd.npc.repository.base.NpcMemberGroupRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.VillageRepository;
import com.cdkhd.npc.service.ThirdService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThirdServiceImpl implements ThirdService {

    private final AreaRepository areaRepository;
    @Autowired
    public ThirdServiceImpl(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }


    @Override
    public AreaVo getRelation(UserDetailsImpl userDetails) {
        AreaVo areaVo = new AreaVo();
        Area area = new Area();//这个地方主要是获取区的信息，不确定用这种方式，暂时还没有写单点登录
        if (userDetails != null) {
            area = userDetails.getArea();
        }else{
            area = areaRepository.findByUid("9ad12abd2dd811ea8f3f0242ac170005");
        }
        List<NodeVo> nodeVos = area.getTowns().stream().filter(town -> town.getStatus().equals(StatusEnum.ENABLED.getValue())).map(town -> NodeVo.convert(town.getUid(),town.getName())).collect(Collectors.toList());//将各镇信息加入节点
        nodeVos.add(0,new NodeVo(area.getUid(),area.getName()));//将区信息加入节点
        //设置节点之间的关系
        List<EdgeVo> edgeVos = Lists.newArrayList();
        for (Town town : area.getTowns()) {
            EdgeVo edgeVo = EdgeVo.convert(area.getUid(),town.getUid());
            edgeVos.add(edgeVo);
        }
        areaVo.setNodes(nodeVos);
        areaVo.setEdges(edgeVos);
        return areaVo;
    }
}
