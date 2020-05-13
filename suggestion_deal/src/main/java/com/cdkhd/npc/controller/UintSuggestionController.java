package com.cdkhd.npc.controller;

import com.cdkhd.npc.service.UnitSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/unit_suggestion")
public class UintSuggestionController {

    private UnitSuggestionService suggestionService;

    @Autowired
    public UintSuggestionController(UnitSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }


}
