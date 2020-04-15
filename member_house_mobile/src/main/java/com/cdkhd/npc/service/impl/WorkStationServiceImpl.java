package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.WorkStation;
import com.cdkhd.npc.entity.vo.WorkStationVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.member_house.WorkStationRepository;
import com.cdkhd.npc.service.WorkStationService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkStationServiceImpl implements WorkStationService {

    private final WorkStationRepository workStationRepository;

    @Autowired
    public WorkStationServiceImpl(WorkStationRepository workStationRepository) {
        this.workStationRepository = workStationRepository;
    }

    @Override
    public RespBody detail(String uid) {
        RespBody body = new RespBody();
        WorkStation workStation = workStationRepository.findByUid(uid);
        if(workStation != null){
            WorkStationVo vo = WorkStationVo.convert(workStation);
            body.setData(vo);
        }else {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该工作站");
        }
        return body;
    }

    @Override
    public RespBody getWorkStations(String uid, Byte level) {
        RespBody body = new RespBody();
        List<WorkStation> workStationList = Lists.newArrayList();
        if (level.equals(LevelEnum.TOWN.getValue())){
            workStationList = workStationRepository.findByTownUidAndLevel(uid, level);
        }else if (level.equals(LevelEnum.AREA.getValue())) {
            workStationList = workStationRepository.findByAreaUidAndLevel(uid, level);
        }
        List<WorkStationVo> stationVos = workStationList.stream().map(station -> WorkStationVo.convert(station)).collect(Collectors.toList());
        body.setData(stationVos);
        return body;
    }
}
