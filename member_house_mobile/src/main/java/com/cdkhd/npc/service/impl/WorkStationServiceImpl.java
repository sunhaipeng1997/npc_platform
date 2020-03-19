package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.WorkStation;
import com.cdkhd.npc.entity.vo.WorkStationVo;
import com.cdkhd.npc.repository.member_house.WorkStationRepository;
import com.cdkhd.npc.service.WorkStationService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
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
}
