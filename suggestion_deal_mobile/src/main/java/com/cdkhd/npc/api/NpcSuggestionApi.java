package com.cdkhd.npc.api;

import com.cdkhd.npc.service.NpcSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal_mobile/npc_suggestion")
public class NpcSuggestionApi {

    private NpcSuggestionService suggestionService;

    @Autowired
    public NpcSuggestionApi(NpcSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }
}
