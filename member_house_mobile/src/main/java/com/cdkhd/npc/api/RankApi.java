package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.StudyTypeDto;
import com.cdkhd.npc.entity.dto.TypeDto;
import com.cdkhd.npc.service.RankService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/rank")
public class RankApi {

    private RankService rankService;

    @Autowired
    public RankApi(RankService rankService) {
        this.rankService = rankService;
    }


    /**
     * 代表建议排行
     * @param userDetails
     * @return
     */
    @GetMapping("/memberSuggestionRank")
    public ResponseEntity memberSuggestionRank(@CurrentUser MobileUserDetailsImpl userDetails, Byte level) {
        RespBody body = rankService.memberSuggestionRank(userDetails, level);
        return ResponseEntity.ok(body);
    }


    /**
     * 各镇建议排行
     * @param userDetails
     * @param typeDto type 1、镇 2 街道
     * @return
     */
    @GetMapping("/townSuggestionRank")
    public ResponseEntity townSuggestionRank(@CurrentUser MobileUserDetailsImpl userDetails, TypeDto typeDto) {
        RespBody body = rankService.townSuggestionRank(userDetails, typeDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 代表收到的意见排行
     * @param userDetails
     * @return
     */
    @GetMapping("/memberOpinionRank")
    public ResponseEntity memberOpinionRank(@CurrentUser MobileUserDetailsImpl userDetails, Byte level) {
        RespBody body = rankService.memberOpinionRank(userDetails, level);
        return ResponseEntity.ok(body);
    }


    /**
     * 各镇收到的意见排行
     * @param userDetails
     * @param typeDto type 1、镇 2 街道
     * @return
     */
    @GetMapping("/townOpinionRank")
    public ResponseEntity townOpinionRank(@CurrentUser MobileUserDetailsImpl userDetails, TypeDto typeDto) {
        RespBody body = rankService.townOpinionRank(userDetails, typeDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 代表履职排行
     * @param userDetails
     * @return
     */
    @GetMapping("/memberPerformanceRank")
    public ResponseEntity memberPerformanceRank(@CurrentUser MobileUserDetailsImpl userDetails, Byte level) {
        RespBody body = rankService.memberPerformanceRank(userDetails, level);
        return ResponseEntity.ok(body);
    }


    /**
     * 各镇履职排行
     * @param userDetails
     * @param typeDto type 1、镇 2 街道
     * @return
     */
    @GetMapping("/townPerformanceRank")
    public ResponseEntity townPerformanceRank(@CurrentUser MobileUserDetailsImpl userDetails, TypeDto typeDto) {
        RespBody body = rankService.townPerformanceRank(userDetails, typeDto);
        return ResponseEntity.ok(body);
    }


}
