package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/manager/member")
public class NpcMemberController {

    private NpcMemberService npcMemberService;

    @Autowired
    public NpcMemberController(NpcMemberService npcMemberService) {
        this.npcMemberService = npcMemberService;
    }

    /**
     * 分页查询代表信息
     * @param userDetails 当前用户
     * @param pageDto 分页查询条件
     * @return 查询结果
     */
    @GetMapping("/page")
    public ResponseEntity pageOfNpcMember(@CurrentUser UserDetailsImpl userDetails, NpcMemberPageDto pageDto) {
        RespBody body = npcMemberService.pageOfNpcMembers(userDetails, pageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加代表
     * @param userDetails 当前用户
     * @param dto 待添加的代表信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity addNpcMember(@CurrentUser UserDetailsImpl userDetails, NpcMemberAddDto dto) {
        RespBody body = npcMemberService.addNpcMember(userDetails, dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改代表信息
     * @param dto 待修改的代表信息
     * @return 修改结果
     */
    @PutMapping
    public ResponseEntity updateNpcMember(NpcMemberAddDto dto) {
        RespBody body = npcMemberService.updateNpcMember(dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 逻辑删除代表信息
     * @param uid 待删除的代表uid
     * @return 删除结果
     */
    @DeleteMapping("/{uid}")
    public ResponseEntity deleteNpcMember(@PathVariable String uid) {
        RespBody body = npcMemberService.deleteNpcMember(uid);
        return ResponseEntity.ok(body);
    }
}
