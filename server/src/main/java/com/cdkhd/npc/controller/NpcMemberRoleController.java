package com.cdkhd.npc.controller;

import com.cdkhd.npc.service.NpcMemberRoleService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/memberRole")
public class NpcMemberRoleController {

    private NpcMemberRoleService npcMemberRoleService;

    @Autowired
    public NpcMemberRoleController(NpcMemberRoleService npcMemberRoleService) {
        this.npcMemberRoleService = npcMemberRoleService;
    }


    /**
     * 查询代表默认必须添加的角色
     * @return 查询结果
     */
    @GetMapping("/getAddRoles")
    public ResponseEntity getAddRoles() {
        RespBody body = npcMemberRoleService.findMustList();
        return ResponseEntity.ok(body);
    }

}
