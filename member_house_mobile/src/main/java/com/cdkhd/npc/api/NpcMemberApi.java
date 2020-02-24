package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/npc_member")
public class NpcMemberApi {

    private NpcMemberService npcMemberService;

    @Autowired
    public NpcMemberApi(NpcMemberService npcMemberService) {
        this.npcMemberService = npcMemberService;
    }


    /**
     * 分页查询代表信息
     * @param userDetails 当前用户
     * @param level 等级
     * @param uid 区、县uid
     * @return 查询结果
     */
    @GetMapping("/pageOfNpcMember")
    public ResponseEntity pageOfNpcMember(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = npcMemberService.allNpcMembers(userDetails, level,uid );
        return ResponseEntity.ok(body);
    }
}
