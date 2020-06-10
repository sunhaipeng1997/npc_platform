package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.UnitAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitPageDto;
import com.cdkhd.npc.entity.dto.UnitUserAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitUserPageDto;
import com.cdkhd.npc.service.IndexService;
import com.cdkhd.npc.service.UnitService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/index")
public class IndexController {

    private IndexService indexService;

    @Autowired
    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }


    /**
     * 政府首页本月数量显示
     */
    @GetMapping("/getSugNumber")
    public ResponseEntity getSugNumber(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.getSugNumber(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 政府首页建议数量趋势图
     */
    @GetMapping("/getSugCount")
    public ResponseEntity getSugCount(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.getSugCount(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 政府首页建议按类型统计图
     */
    @GetMapping("/sugBusinessLine")
    public ResponseEntity sugBusinessLine(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.sugBusinessLine(userDetails);
        return ResponseEntity.ok(body);
    }


    /**
     * 政府首页建议办理中的建议统计图
     */
    @GetMapping("/sugUnitDealingLine")
    public ResponseEntity sugUnitDealingLine(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.sugUnitDealingLine(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 政府首页建议办结的建议统计图
     */
    @GetMapping("/sugUnitCompletedLine")
    public ResponseEntity sugUnitCompletedLine(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.sugUnitCompletedLine(userDetails);
        return ResponseEntity.ok(body);
    }
}
