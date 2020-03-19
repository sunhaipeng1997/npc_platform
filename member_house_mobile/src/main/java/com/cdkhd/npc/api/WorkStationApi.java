package com.cdkhd.npc.api;

import com.cdkhd.npc.service.WorkStationService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/work_station")
public class WorkStationApi {

    private final WorkStationService workStationService;

    @Autowired
    public WorkStationApi(WorkStationService workStationService) {
        this.workStationService = workStationService;
    }

    @GetMapping("/detail")
    public ResponseEntity detail(String uid){
        RespBody body = workStationService.detail(uid);
        return ResponseEntity.ok(body);
    }
}
