package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.service.NpcSuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal_mobile/npc_suggestion")
public class NpcSuggestionApi {

    private NpcSuggestionService suggestionService;

    @Autowired
    public NpcSuggestionApi(NpcSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
    * @Description: 获取提建议的类型下拉列表项
    * @Param: userDetails sugBusDto
    * @Return:
    * @Date: 2020/5/19
    * @Author: LiYang
    */
    @GetMapping("/sugBusList")
    public ResponseEntity sugBusList(@CurrentUser MobileUserDetailsImpl userDetails, SugBusDto sugBusDto){
        RespBody body = suggestionService.sugBusList(userDetails, sugBusDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 代表添加建议
    * @Param: userDetails sugAddDto
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @PostMapping("/addSuggestion")
    public ResponseEntity addSuggestion(@CurrentUser MobileUserDetailsImpl userDetails, SugAddDto sugAddDto){
        RespBody body = suggestionService.addSuggestion(userDetails, sugAddDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 代表修改建议
    * @Param: sugAddDto
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @PostMapping("/updateSuggestion")
    public ResponseEntity updateSuggestion(SugAddDto sugAddDto){
        RespBody body = suggestionService.updateSuggestion(sugAddDto);
        return ResponseEntity.ok(body);
    }
    /**
    * @Description: 将草稿箱中的建议提交审核
    * @Param: sugUid
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @PostMapping("/submitSuggestion")
    public ResponseEntity submitSuggestion(SugAddDto sugAddDto){
        RespBody body = suggestionService.submitSuggestion(sugAddDto);
        return ResponseEntity.ok(body);
    }
    /**
    * @Description: 撤回建议
    * @Param: sugUid
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @PostMapping("/revokeSuggestion")
    public ResponseEntity revokeSuggestion(String sugUid){
        RespBody body = suggestionService.revokeSuggestion(sugUid);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 查看建议详情
    * @Param: sugUid
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @GetMapping("/suggestionDetail")
    public ResponseEntity suggestionDetail(@CurrentUser MobileUserDetailsImpl userDetails, ViewDto viewDto){
        RespBody body = suggestionService.suggestionDetail(userDetails, viewDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 删除建议 包括撤回后、审核不通过、草稿箱中的建议可以删除
    * @Param: sugUid
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @DeleteMapping("/deleteSuggestion")
    public ResponseEntity deleteSuggestion(String sugUid){
        RespBody body = suggestionService.deleteSuggestion(sugUid);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 审核人员审核建议
    * @Param: userDetails
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @PostMapping("/auditSuggestion")
    public ResponseEntity auditSuggestion(@CurrentUser MobileUserDetailsImpl userDetails, SugAuditDto sugAuditDto) {
        RespBody body = suggestionService.auditSuggestion(userDetails, sugAuditDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 代表提出的建议列表
    * @Param: userDetails sugPageDto
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    @GetMapping("/npcMemberSug")
    public ResponseEntity npcMemberSug(@CurrentUser MobileUserDetailsImpl userDetails, SugPageDto sugPageDto) {
        RespBody body = suggestionService.npcMemberSug(userDetails, sugPageDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 审核人员建议列表
    * @Param: userDetails sugPageDto
    * @Return:
    * @Date: 2020/5/19
    * @Author: LiYang
    */
    @GetMapping("/auditorSug")
    public ResponseEntity auditorSug(@CurrentUser MobileUserDetailsImpl userDetails, SugPageDto sugPageDto) {
        RespBody body = suggestionService.auditorSug(userDetails, sugPageDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 代表接受办理结果
    * @Param: userDetails sugAppraiseDto
    * @Return:
    * @Date: 2020/5/26
    * @Author: LiYang
    */
    @PostMapping("/acceptResult")
    public ResponseEntity acceptResult(@CurrentUser MobileUserDetailsImpl userDetails, SugAppraiseDto sugAppraiseDto){
        RespBody body = suggestionService.acceptResult(userDetails, sugAppraiseDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 代表不接受办理结果
    * @Param: sugAppraiseDto
    * @Return:
    * @Date: 2020/5/26
    * @Author: LiYang
    */
    @PostMapping("/refuseResult")
    public ResponseEntity refuseResult(SugAppraiseDto sugAppraiseDto){
        RespBody body = suggestionService.refuseResult(sugAppraiseDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 代表附议建议
    * @Param: userDetails sugSecondDto
    * @Return:
    * @Date: 2020/5/28
    * @Author: LiYang
    */
    @PostMapping("/secondSuggestion")
    public ResponseEntity secondSuggestion(@CurrentUser MobileUserDetailsImpl userDetails, SugSecondDto sugSecondDto){
        RespBody body = suggestionService.secondSuggestion(userDetails, sugSecondDto);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 代表催办建议
    * @Param: userDetails sugUid
    * @Return:
    * @Date: 2020/5/29
    * @Author: LiYang
    */
    @PostMapping("/urgeSuggestion")
    public ResponseEntity urgeSuggestion(@CurrentUser MobileUserDetailsImpl userDetails, String sugUid){
        RespBody body = suggestionService.urgeSuggestion(userDetails, sugUid);
        return ResponseEntity.ok(body);
    }
}
