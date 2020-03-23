package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.enums.LevelEnum;
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

    //获取我任职所在地的工作站
    @GetMapping("/getWorkStations")
    public ResponseEntity getWorkStations(@CurrentUser UserDetailsImpl userDetails, Byte level){
        String uid = "";
        if (level.equals(LevelEnum.TOWN.getValue())){
            uid = userDetails.getTown().getUid();
        }else if (level.equals(LevelEnum.AREA.getValue())){
            uid = userDetails.getArea().getUid();
        }
        RespBody body = workStationService.getWorkStations(uid,level);
        return ResponseEntity.ok(body);
    }

    //根据uid获取该机构所有的工作站
    @GetMapping("/getWorkStationList")
    public ResponseEntity getWorkStationList(String uid,Byte level){
        RespBody body = workStationService.getWorkStations(uid,level);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/detail")
    public ResponseEntity detail(String uid){
        RespBody body = workStationService.detail(uid);
        return ResponseEntity.ok(body);
    }
}
