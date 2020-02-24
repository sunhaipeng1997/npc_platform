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
     * @param
     * @param level 等级
     * @param uid 区、县uid
     * @return 查询结果
     */
    @GetMapping("/allNpcMembers")
    public ResponseEntity allNpcMembers(Byte level, String uid) {
        RespBody body = npcMemberService.allNpcMembers(level,uid);
        return ResponseEntity.ok(body);
    }



    /**
     * 获取当前区域下的行政列表
     * @param userDetails
     * @param level 等级 区 返回贞烈镇列表 镇 返回小组列表
     * @param uid 区、县uid
     * @return 查询结果
     */
    @GetMapping("/npcMemberUnits")
    public ResponseEntity npcMemberUnits(@CurrentUser UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody body = npcMemberService.npcMemberUnits(userDetails, level,uid);
        return ResponseEntity.ok(body);
    }
}
