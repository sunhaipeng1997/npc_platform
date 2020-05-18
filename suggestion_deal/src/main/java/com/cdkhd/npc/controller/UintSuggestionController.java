package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.UnitSuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    ResponseEntity pageToDeal(@CurrentUser UserDetailsImpl userDetails, SuggestionDto dto) {
        RespBody body = suggestionService.findToDeal(userDetails, dto);
        return ResponseEntity.ok(body);
    }
}
