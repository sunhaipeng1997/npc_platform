package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.GovSuggestionPageDto;
import com.cdkhd.npc.entity.dto.ToDealPageDto;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.UnitSuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    /*@GetMapping("/page_in_dealing")
    ResponseEntity pageInDealing(@CurrentUser UserDetailsImpl userDetails, ToDealPageDto pageDto) {
        RespBody body = suggestionService.findToDeal(userDetails, pageDto);
        return ResponseEntity.ok(body);
    }*/
}
