package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionBusinessAddDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessDto;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.service.SuggestionService;
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
@RequestMapping("/api/member_house/suggestion")
public class SuggestionController {

    private final SuggestionService suggestionService;

    @Autowired
    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
     * 获取所有建议业务类型下拉列表
     */
    @GetMapping("/sugBusList")
    public ResponseEntity sugBusList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = suggestionService.sugBusList(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取某镇所有建议业务类型下拉列表
     */
    @GetMapping("/subTownBusList")
    public ResponseEntity subTownBusList(String townUid) {
        RespBody body = suggestionService.subTownBusList(townUid);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取建议业务类型列表（后台）
     *
     * @return
     */
    @GetMapping("/suggestionBusiness")
    public ResponseEntity suggestionBusiness(@CurrentUser UserDetailsImpl userDetails, SuggestionBusinessDto suggestionBusinessDto) {
        RespBody body = suggestionService.findSuggestionBusiness(userDetails, suggestionBusinessDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加、修改建议业务类型
     *
     * @return
     */
    @PostMapping("/addOrUpdateSuggestionBusiness")
    public ResponseEntity addOrUpdateSuggestionBusiness(@CurrentUser UserDetailsImpl userDetails, SuggestionBusinessAddDto suggestionBusinessAddDto) {
        RespBody body = suggestionService.addOrUpdateSuggestionBusiness(userDetails, suggestionBusinessAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除建议业务类型
     *
     * @return
     */
    @DeleteMapping("/deleteSuggestionBusiness")
    public ResponseEntity deleteSuggestionBusiness(String uid) {
        RespBody body = suggestionService.deleteSuggestionBusiness(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改业务类型排序
     *
     * @return
     */
    @PostMapping("/changeTypeSequence")
    public ResponseEntity changeTypeSequence(@CurrentUser UserDetailsImpl userDetails, String uid, Byte type) {
        RespBody body = suggestionService.changeTypeSequence(userDetails, uid, type);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改业务类型状态
     *
     * @return
     */
    @PostMapping("/changeBusinessStatus")
    public ResponseEntity changeBusinessStatus(String uid, Byte status) {
        RespBody body = suggestionService.changeBusinessStatus(uid, status);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取已提建议信息列表
     *
     * @return
     */
    @GetMapping("/findSuggestion")
    public ResponseEntity findSuggestion(@CurrentUser UserDetailsImpl userDetails, SuggestionDto suggestionDto) {
        RespBody body = suggestionService.findSuggestion(userDetails, suggestionDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 导出建议列表信息
     *
     * @return
     */
    @GetMapping("/exportSuggestion")
    public void exportSuggestion(@CurrentUser UserDetailsImpl userDetails, SuggestionDto suggestionDto, HttpServletRequest req, HttpServletResponse res) {
        suggestionService.exportSuggestion(userDetails, suggestionDto, req, res);
    }

    /**
     * 获取已提建议信息列表
     * @return
     */
    @GetMapping("/countSuggestion")
    public ResponseEntity countSuggestion(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = suggestionService.countSuggestion(userDetails);
        return ResponseEntity.ok(body);
    }

}
