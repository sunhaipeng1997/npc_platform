package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.entity.vo.EdgeVo;
import com.cdkhd.npc.entity.vo.NodeVo;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AreaRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.OpinionRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.ThirdService;
import com.cdkhd.npc.vo.CountVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ThirdServiceImpl implements ThirdService {

    private final AreaRepository areaRepository;
    private SuggestionRepository suggestionRepository;
    private OpinionRepository opinionRepository;
    private PerformanceRepository performanceRepository;
    private NpcMemberRepository npcMemberRepository;

    @Autowired
    public ThirdServiceImpl(AreaRepository areaRepository, SuggestionRepository suggestionRepository, OpinionRepository opinionRepository, PerformanceRepository performanceRepository, NpcMemberRepository npcMemberRepository) {
        this.areaRepository = areaRepository;
        this.suggestionRepository = suggestionRepository;
        this.opinionRepository = opinionRepository;
        this.performanceRepository = performanceRepository;
        this.npcMemberRepository = npcMemberRepository;
    }

    //获取区及其包含的镇
    @Override
    public AreaVo getRelation(UserDetailsImpl userDetails) {
        AreaVo areaVo = new AreaVo();
        Area area = userDetails.getArea();
        List<NodeVo> nodeVos = area.getTowns().stream().filter(town -> town.getStatus().equals(StatusEnum.ENABLED.getValue()) && !town.getIsDel()).map(town -> NodeVo.convert(town.getUid(),town.getName())).collect(Collectors.toList());//将各镇信息加入节点
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

    //统计各镇的建议数量
    @Override
    public RespBody countSuggestions4Town(UserDetailsImpl userDetails) {
        RespBody<List<CountVo>> body = new RespBody<>();
        body.setData(suggestionRepository.count4Town(userDetails.getArea().getId()));
        return body;
    }

    //统计各镇的意见数量
    @Override
    public RespBody countOpinions4Town(UserDetailsImpl userDetails) {
        RespBody<List<CountVo>> body = new RespBody<>();
        body.setData(opinionRepository.count4Town(userDetails.getArea().getId()));
        return body;
    }

    //统计各镇的履职数量
    @Override
    public RespBody countPerformances4Town(UserDetailsImpl userDetails) {
        RespBody<List<CountVo>> body = new RespBody<>();
        body.setData(performanceRepository.count4Town(userDetails.getArea().getId()));
        return body;
    }

    //统计各类型的建议数量
    @Override
    public RespBody countSuggestions4Type(UserDetailsImpl userDetails) {
        RespBody<List<CountVo>> body = new RespBody<>();
        body.setData(suggestionRepository.count4Type(userDetails.getArea().getId()));
        return body;
    }

    //统计代表的学历
    @Override
    public RespBody countEducation4NpcMember(UserDetailsImpl userDetails) {
        RespBody<List<CountVo>> body = new RespBody<>();
        body.setData(npcMemberRepository.countEducation(userDetails.getArea().getId()));
        return body;
    }

    //统计各镇的建议数量之和，意见数量之和，履职数量之和
    @Override
    public RespBody countAll(UserDetailsImpl userDetails) {
        RespBody<JSONObject> body = new RespBody<>();

        JSONObject object = new JSONObject();
        object.put("suggestion", suggestionRepository.countAll(userDetails.getArea().getId()));
        object.put("performance", performanceRepository.countAll(userDetails.getArea().getId()));
        object.put("opinion", opinionRepository.countAll(userDetails.getArea().getId()));

        body.setData(object);
        return body;
    }
}
