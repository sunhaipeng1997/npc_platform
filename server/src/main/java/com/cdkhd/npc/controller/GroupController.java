package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.GroupAddDto;
import com.cdkhd.npc.entity.dto.GroupPageDto;
import com.cdkhd.npc.service.GroupService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house/group")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * 查询小组列表
     *
     * @return
     */
    @GetMapping
    public ResponseEntity page(@CurrentUser UserDetailsImpl userDetails, GroupPageDto groupPageDto){
        RespBody body = groupService.page(userDetails, groupPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 查询某个小组详细信息
     *
     * @return
     */
    @GetMapping("/details")
    public ResponseEntity details(String uid){
        RespBody body = groupService.details(uid);
        return ResponseEntity.ok(body);
    }


    /**
     * 增加小组
     *
     * @return
     */
    @PostMapping
    public ResponseEntity add(@CurrentUser UserDetailsImpl userDetails, GroupAddDto groupAddDto){
        RespBody body = groupService.add(userDetails, groupAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改小组
     *
     * @return
     */
    @PostMapping("/update")
    public ResponseEntity update(@CurrentUser UserDetailsImpl userDetails, GroupAddDto groupAddDto){
        RespBody body = groupService.update(userDetails, groupAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除小组
     *
     * @return
     */
    @DeleteMapping
    public ResponseEntity delete(String uid){
        RespBody body = groupService.delete(uid);
        return ResponseEntity.ok(body);
    }
}
