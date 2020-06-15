package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionBusinessAddDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessDto;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.service.NpcSuggestionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/suggestion_deal/npc_suggestion")
public class NpcSuggestionController {

    private  NpcSuggestionService npcSuggestionService;

    @Autowired
    public NpcSuggestionController(NpcSuggestionService npcSuggestionService) {
        this.npcSuggestionService = npcSuggestionService;
    }

    /**
     * 获取所有建议业务类型下拉列表
     */
    @GetMapping("/sugBusList")
    public ResponseEntity sugBusList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = npcSuggestionService.sugBusList(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取建议业务类型列表（后台）
     *
     * @return
     */
    @GetMapping("/suggestionBusiness")
    public ResponseEntity suggestionBusiness(@CurrentUser UserDetailsImpl userDetails, SuggestionBusinessDto suggestionBusinessDto) {
        RespBody body = npcSuggestionService.findSuggestionBusiness(userDetails, suggestionBusinessDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加、修改建议业务类型
     *
     * @return
     */
    @PostMapping("/addOrUpdateSuggestionBusiness")
    public ResponseEntity addOrUpdateSuggestionBusiness(@CurrentUser UserDetailsImpl userDetails, SuggestionBusinessAddDto suggestionBusinessAddDto) {
        RespBody body = npcSuggestionService.addOrUpdateSuggestionBusiness(userDetails, suggestionBusinessAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除建议业务类型
     *
     * @return
     */
    @DeleteMapping("/deleteSuggestionBusiness")
    public ResponseEntity deleteSuggestionBusiness(String uid) {
        RespBody body = npcSuggestionService.deleteSuggestionBusiness(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改业务类型状态
     *
     * @return
     */
    @PostMapping("/changeBusinessStatus")
    public ResponseEntity changeBusinessStatus(String uid, Byte status) {
        RespBody body = npcSuggestionService.changeBusinessStatus(uid, status);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改业务类型排序
     *
     * @return
     */
    @PostMapping("/changeTypeSequence")
    public ResponseEntity changeTypeSequence(@CurrentUser UserDetailsImpl userDetails, String uid, Byte type) {
        RespBody body = npcSuggestionService.changeTypeSequence(userDetails, uid, type);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取已提建议信息列表
     *
     * @return
     */
    @GetMapping("/findSuggestion")
    public ResponseEntity findSuggestion(@CurrentUser UserDetailsImpl userDetails, SuggestionDto suggestionDto) {
        RespBody body = npcSuggestionService.findSuggestion(userDetails, suggestionDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 导出建议列表信息
     *
     * @return
     */
    @GetMapping("/exportSuggestion")
    public void exportSuggestion(@CurrentUser UserDetailsImpl userDetails, SuggestionDto suggestionDto, HttpServletRequest req, HttpServletResponse res) {
        npcSuggestionService.exportSuggestion(userDetails, suggestionDto, req, res);
    }
}
