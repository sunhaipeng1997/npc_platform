package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.service.ThirdService;
import com.cdkhd.npc.vo.CountVo;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//作为大数据展示平台的数据查询接口
@RestController
@RequestMapping("/api/manager/third")
public class ThirdController {

    private  ThirdService thirdService;

    @Autowired
    public ThirdController(ThirdService thirdService) {
        this.thirdService = thirdService;
    }

    /**
     * 查询镇列表
     *
     * @return
     */
    @GetMapping("/getRelation")
    public AreaVo getRelation(@CurrentUser UserDetailsImpl userDetails){
        return thirdService.getRelation(userDetails);
    }

    /**
     * 统计各镇 / 小组的建议数量
     *
     * @param userDetails
     * @param level 区 / 镇
     * @param uid 区uid / 镇uid
     * @return
     */
    @GetMapping("/sugNumberByTown")
    public ResponseEntity countSug4Town(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = thirdService.countSuggestions4Town(userDetails, level, uid);
        return ResponseEntity.ok(body);
    }

    //统计各镇的意见数量
    @GetMapping("/opnNumberByTown")
    public ResponseEntity countOpn4Town(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = thirdService.countOpinions4Town(userDetails, level, uid);
        return ResponseEntity.ok(body);
    }

    //统计各镇的履职数量
    @GetMapping("/pfmNumberByTown")
    public ResponseEntity countPfm4Town(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = thirdService.countPerformances4Town(userDetails, level, uid);
        return ResponseEntity.ok(body);
    }

    //统计各类型的建议数量
    @GetMapping("/sugNumberByType")
    public ResponseEntity countSug4Type(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = thirdService.countSuggestions4Type(userDetails, level, uid);
        return ResponseEntity.ok(body);
    }

    //统计代表的学历
    @GetMapping("/npcNumberByEducation")
    public ResponseEntity countEdu4Npc(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = thirdService.countEducation4NpcMember(userDetails, level, uid);
        return ResponseEntity.ok(body);
    }

    //统计各镇的建议数量之和，意见数量之和，履职数量之和
    @GetMapping("/allSugPfmOpn")
    public ResponseEntity countAll(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = thirdService.countAll(userDetails, level, uid);
        return ResponseEntity.ok(body);
    }
}
