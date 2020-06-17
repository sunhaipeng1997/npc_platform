package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.UnitSuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/suggestion_deal/unit_suggestion")
public class UintSuggestionController {

    private GeneralService generalService;
    private UnitSuggestionService suggestionService;

    @Autowired
    public UintSuggestionController(GeneralService generalService, UnitSuggestionService suggestionService) {
        this.generalService = generalService;
        this.suggestionService = suggestionService;
    }

    @GetMapping("/sug_type")
    ResponseEntity findSugType(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = generalService.findSugBusiness(userDetails);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/page_to_deal")
    ResponseEntity pageToDeal(@CurrentUser UserDetailsImpl userDetails, ToDealPageDto pageDto) {
        RespBody body = suggestionService.findToDeal(userDetails, pageDto);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/adjust/{uid}")
    public ResponseEntity applyAdjustUnit(@CurrentUser UserDetailsImpl userDetails, @PathVariable("uid") String cpUid, String adjustReason) {
        RespBody body = suggestionService.applyAdjust(userDetails, cpUid, adjustReason);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/receive/{uid}")
    public ResponseEntity startDealing(@CurrentUser UserDetailsImpl userDetails, @PathVariable("uid") String cpUid) {
        RespBody body = suggestionService.startDealing(userDetails, cpUid);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/page_in_dealing")
    ResponseEntity pageInDealing(@CurrentUser UserDetailsImpl userDetails, InDealingPageDto pageDto) {
        RespBody body = suggestionService.findPageOfInDealing(userDetails, pageDto);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/delay/{uid}")
    public ResponseEntity applyDelay(@CurrentUser UserDetailsImpl userDetails, @PathVariable("uid") String usUid,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date delayUntil, String reason) {
        RespBody body = suggestionService.applyDelay(userDetails, usUid, delayUntil, reason);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/image")
    public ResponseEntity uploadOneImage(@CurrentUser UserDetailsImpl userDetails, MultipartFile image, Byte type) {
        RespBody body = suggestionService.uploadOneImage(userDetails, image, type);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/process")
    public ResponseEntity addProcess(@CurrentUser UserDetailsImpl userDetails, HandleProcessAddDto toAdd) {
        RespBody body = suggestionService.addHandleProcess(userDetails, toAdd);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/finish")
    public ResponseEntity finishDeal(@CurrentUser UserDetailsImpl userDetails, ResultAddDto toAdd) {
        RespBody body = suggestionService.finishDeal(userDetails, toAdd);
        return ResponseEntity.ok(body);
    }
}
