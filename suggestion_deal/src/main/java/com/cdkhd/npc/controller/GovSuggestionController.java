package com.cdkhd.npc.controller;

import com.cdkhd.npc.service.GovSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/gov_suggestion")
public class GovSuggestionController {

    private GovSuggestionService suggestionService;

    @Autowired
    public GovSuggestionController(GovSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }


}
