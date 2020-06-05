package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.*;
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
@RequestMapping("/api/suggestion_deal_mobile/gov_suggestion")
public class GovSuggestionApi {

    private GovSuggestionService suggestionService;

    @Autowired
    public GovSuggestionApi(GovSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
     * 条件查询建议列表
     */
    @GetMapping("/getGovSuggestion")
    public ResponseEntity getGovSuggestion(@CurrentUser MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = suggestionService.getGovSuggestion(userDetails, govSuggestionPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 转办建议
     */
    @PostMapping("/conveySuggestion")
    public ResponseEntity conveySuggestion(@CurrentUser MobileUserDetailsImpl userDetails, ConveySuggestionDto conveySuggestionDto) {
        RespBody body = suggestionService.conveySuggestion(userDetails, conveySuggestionDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 转办建议
     */
    @PostMapping("/adjustConvey")
    public ResponseEntity adjustConvey(@CurrentUser MobileUserDetailsImpl userDetails, AdjustConveyDto adjustConveyDto) {
        RespBody body = suggestionService.adjustConvey(userDetails, adjustConveyDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 延期建议
     */
    @PostMapping("/delaySuggestion")
    public ResponseEntity delaySuggestion(@CurrentUser MobileUserDetailsImpl userDetails, DelaySuggestionDto delaySuggestionDto) {
        RespBody body = suggestionService.delaySuggestion(userDetails, delaySuggestionDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 获取建议详情
     */
    @GetMapping("/getSuggestionDetail")
    public ResponseEntity getSuggestionDetail(BaseDto baseDto) {
        RespBody body = suggestionService.getSuggestionDetail(baseDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 条件查询申请重新转办的建议列表
     */
    @GetMapping("/applyConvey")
    public ResponseEntity applyConvey(@CurrentUser MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = suggestionService.applyConvey(userDetails, govSuggestionPageDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 获取申请重新转办的建议详情
     */
    @GetMapping("/getAdjustSugDetail")
    public ResponseEntity getAdjustSugDetail(BaseDto baseDto) {
        RespBody body = suggestionService.getAdjustSugDetail(baseDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 条件查询申请延期的建议列表
     */
    @GetMapping("/applyDelay")
    public ResponseEntity applyDelay(@CurrentUser MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = suggestionService.applyDelay(userDetails, govSuggestionPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取申请延期的建议详情
     */
    @GetMapping("/getDelaySugDetail")
    public ResponseEntity getDelaySugDetail(BaseDto baseDto) {
        RespBody body = suggestionService.getDelaySugDetail(baseDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 条件查询申请延期的建议列表
     */
    @PostMapping("/urgeSug")
    public ResponseEntity urgeSug(@CurrentUser MobileUserDetailsImpl userDetails, LevelDto levelDto) {
        RespBody body = suggestionService.urgeSug(userDetails, levelDto);
        return ResponseEntity.ok(body);
    }
}
