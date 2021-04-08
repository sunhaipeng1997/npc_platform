package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.service.SuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/suggestion")
public class SuggestionApi {

    private final SuggestionService suggestionService;

    @Autowired
    public SuggestionApi(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }


    /**
     * 根据代表身份
     * 获取建议业务下拉类型列表
     * @param userDetails
     * @return
     * */
    @GetMapping("/sugBusList")
    public ResponseEntity sugBusList(@CurrentUser MobileUserDetailsImpl userDetails, SuggestionBusinessDto dto){
        RespBody body = suggestionService.sugBusList(userDetails, dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 根据代表身份、查询条件获取提出
     * 的建议列表
     * @param userDetails
     * @param dto
     * @return
     */
    @GetMapping("/npcMemberSug")
    public ResponseEntity npcMemberSug(@CurrentUser MobileUserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody body = suggestionService.npcMemberSug(userDetails, dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加或修改建议
     * @param userDetails
     * @param suggestionAddDto
     * */
    @PostMapping("/addOrUpdateSuggestion")
    public ResponseEntity addOrUpdateSuggestion(@CurrentUser MobileUserDetailsImpl userDetails, SuggestionAddDto suggestionAddDto) {
        RespBody body = suggestionService.addOrUpdateSuggestion(userDetails,suggestionAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 根据建议uid查询建议详情
     * @param viewDto
     * @return
     * */
    @GetMapping("/suggestionDetail")
    public ResponseEntity suggestionDetail(ViewDto viewDto){
        RespBody body = suggestionService.suggestionDetail(viewDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 审核人员审核建议
     * @param userDetails
     * @param suggestionAuditDto
     * */
    @PostMapping("/audit")
    public ResponseEntity audit(@CurrentUser MobileUserDetailsImpl userDetails, SuggestionAuditDto suggestionAuditDto) {
        RespBody body = suggestionService.audit(userDetails, suggestionAuditDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除建议
     * @param uid
     * @return
     *
     * */
    @DeleteMapping("/deleteSuggestion")
    public ResponseEntity delete(String uid) {
        RespBody body = suggestionService.deleteSuggestion(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 撤回建议
     * @param uid
     * @return
     *
     * */
    @GetMapping("/revoke")
    public ResponseEntity suggestionRevoke(String uid) {
        RespBody body = suggestionService.suggestionRevoke(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 审核人员待审核的建议
     * @param userDetails
     * @param dto
     * */
    @GetMapping("/auditorSug")
    public ResponseEntity auditorSug(@CurrentUser MobileUserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody body = suggestionService.auditorSug(userDetails, dto);
        return ResponseEntity.ok(body);
    }

    /**
     *  根据选择的代表查看其提出的建议
     * @param uid
     * @return
     */
    @GetMapping("/getMemberSugList")
    public ResponseEntity getMemberSugList(String uid, SuggestionPageDto dto){
        RespBody body = suggestionService.getMemberSugList(uid, dto);
        return ResponseEntity.ok(body);
    }
}
