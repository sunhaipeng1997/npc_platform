package com.cdkhd.npc.api;

import com.cdkhd.npc.service.GovSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal_mobile/gov_suggestion")
public class GovSuggestionApi {

    private GovSuggestionService suggestionService;

    @Autowired
    public GovSuggestionApi(GovSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }
}
