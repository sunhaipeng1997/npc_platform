package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionAddDto;
import com.cdkhd.npc.entity.dto.SuggestionAuditDto;
import com.cdkhd.npc.entity.dto.SuggestionPageDto;
import com.cdkhd.npc.service.SuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/member_house_mobile/suggestion")
public class SuggestionApi {

    private final SuggestionService suggestionService;

    @Autowired
    public SuggestionApi(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }


    /**
     * 获取所有建议业务类型列表
     *
     * */
    @GetMapping("/sugBusList")
    public ResponseEntity sugBusList(@CurrentUser UserDetailsImpl userDetails){
        RespBody body = suggestionService.sugBusList(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 代表提出的建议
     *
     * @param userDetails
     * @param dto
     * @return
     */
    @GetMapping("/npcMemberSug")
    public ResponseEntity npcMemberSug(@CurrentUser UserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody body = suggestionService.npcMemberSug(userDetails, dto);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/addOrUpdateSuggestion")
    public ResponseEntity addOrUpdateSuggestion(@CurrentUser UserDetailsImpl userDetails, SuggestionAddDto suggestionAddDto) {
        RespBody body = suggestionService.addOrUpdateSuggestion(userDetails,suggestionAddDto);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/detail")
    public ResponseEntity suggestionDetail(String uid){
        String result = suggestionService.suggestionDetail(uid);
        RespBody body = new RespBody();
        body.setData(result);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/audit")
    public ResponseEntity audit(@CurrentUser UserDetailsImpl userDetails, SuggestionAuditDto suggestionAuditDto) {
        RespBody body = suggestionService.audit(userDetails, suggestionAuditDto);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping
    public ResponseEntity delete(@RequestBody SuggestionAddDto suggestionAddDto) {
        RespBody body = suggestionService.deleteSuggestion(suggestionAddDto.getUid());
        return ResponseEntity.ok(body);
    }

    /**
     * 撤回建议
     *
     * */
    @GetMapping("/revoke")
    public ResponseEntity suggestionRevoke(String uid) {
        RespBody body = suggestionService.suggestionRevoke(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 审核人员待审核的建议
     * */
    @GetMapping("/auditorSug")
    public ResponseEntity auditorSug(@CurrentUser UserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody body = suggestionService.auditorSug(userDetails, dto);
        return ResponseEntity.ok(body);
    }
}
