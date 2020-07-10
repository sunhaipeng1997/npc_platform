package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.AdjustConveyDto;
import com.cdkhd.npc.entity.dto.ConveySuggestionDto;
import com.cdkhd.npc.entity.dto.DelaySuggestionDto;
import com.cdkhd.npc.entity.dto.GovSuggestionPageDto;
import com.cdkhd.npc.service.GovSuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/suggestion_deal/gov_suggestion")
public class GovSuggestionController {

    private GovSuggestionService suggestionService;

    @Autowired
    public GovSuggestionController(GovSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
     * 条件查询建议列表
     */
    @GetMapping("/getGovSuggestion")
    public ResponseEntity getGovSuggestion(@CurrentUser UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = suggestionService.getGovSuggestion(userDetails, govSuggestionPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 转办建议
     */
    @PostMapping("/conveySuggestion")
    public ResponseEntity conveySuggestion(@CurrentUser UserDetailsImpl userDetails, ConveySuggestionDto conveySuggestionDto) {
        RespBody body = suggestionService.conveySuggestion(userDetails, conveySuggestionDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 转办建议
     */
    @PostMapping("/adjustConvey")
    public ResponseEntity adjustConvey(@CurrentUser UserDetailsImpl userDetails, AdjustConveyDto adjustConveyDto) {
        RespBody body = suggestionService.adjustConvey(userDetails, adjustConveyDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 延期建议
     */
    @PostMapping("/delaySuggestion")
    public ResponseEntity delaySuggestion(@CurrentUser UserDetailsImpl userDetails, DelaySuggestionDto delaySuggestionDto) {
        RespBody body = suggestionService.delaySuggestion(userDetails, delaySuggestionDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 导出建议信息
     */
    @GetMapping("/exportGovSuggestion")
    public void exportGovSuggestion(@CurrentUser UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, HttpServletRequest req, HttpServletResponse res) {
        suggestionService.exportGovSuggestion(userDetails, govSuggestionPageDto,req,res);
    }


    /**
     * 条件查询申请重新转办的建议列表
     */
    @GetMapping("/applyConvey")
    public ResponseEntity applyConvey(@CurrentUser UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = suggestionService.applyConvey(userDetails, govSuggestionPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 条件查询申请延期的建议列表
     */
    @GetMapping("/applyDelay")
    public ResponseEntity applyDelay(@CurrentUser UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = suggestionService.applyDelay(userDetails, govSuggestionPageDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 条件查询申请延期的建议列表
     */
    @PostMapping("/urgeSug")
    public ResponseEntity urgeSug(@CurrentUser UserDetailsImpl userDetails, BaseDto baseDto) {
        RespBody body = suggestionService.urgeSug(userDetails, baseDto);
        return ResponseEntity.ok(body);
    }
}
