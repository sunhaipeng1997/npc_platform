package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.PageDto;
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
@RequestMapping("/api/suggestion_deal_mobile/unit")
public class UnitSuggestionApi {

    private UnitSuggestionService unitSuggestionService;

    @Autowired
    public UnitSuggestionApi(UnitSuggestionService unitSuggestionService) {
        this.unitSuggestionService = unitSuggestionService;
    }

    @GetMapping("/page_to_deal")
    public ResponseEntity findPageOfToDeal(@CurrentUser MobileUserDetailsImpl userDetails, PageDto pageDto) {
        RespBody body = unitSuggestionService.findPageOfToDeal(userDetails, pageDto);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/to_deal/{uid}")
    public ResponseEntity checkToDeal(@CurrentUser MobileUserDetailsImpl userDetails, @PathVariable("uid") String uid) {
        RespBody body = unitSuggestionService.checkToDealDetail(userDetails, uid);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/adjust/{uid}")
    public ResponseEntity applyAdjustUnit(@CurrentUser MobileUserDetailsImpl userDetails, @PathVariable("uid") String cpUid, String adjustReason) {
        RespBody body = unitSuggestionService.applyAdjust(userDetails, cpUid, adjustReason);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/receive/{uid}")
    public ResponseEntity startDealing(@CurrentUser MobileUserDetailsImpl userDetails, @PathVariable("uid") String cpUid) {
        RespBody body = unitSuggestionService.startDealing(userDetails, cpUid);
        return ResponseEntity.ok(body);
    }
}
