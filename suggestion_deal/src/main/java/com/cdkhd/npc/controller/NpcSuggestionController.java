package com.cdkhd.npc.controller;

import com.cdkhd.npc.service.NpcSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/npc_suggestion")
public class NpcSuggestionController {

    private  NpcSuggestionService suggestionService;

    @Autowired
    public NpcSuggestionController(NpcSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }


}
