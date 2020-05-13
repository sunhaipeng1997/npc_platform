package com.cdkhd.npc.api;

import com.cdkhd.npc.service.UnitSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal_mobile/npc_suggestion")
public class UnitSuggestionApi {

    private UnitSuggestionService suggestionService;

    @Autowired
    public UnitSuggestionApi(UnitSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }
}
