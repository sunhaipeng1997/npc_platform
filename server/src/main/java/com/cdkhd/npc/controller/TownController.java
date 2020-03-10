package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.TownAddDto;
import com.cdkhd.npc.entity.dto.TownPageDto;
import com.cdkhd.npc.service.TownService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/town")
public class TownController {

    private final TownService townService;

    @Autowired
    public TownController(TownService townService) {
        this.townService = townService;
    }

    /**
     * 查询镇列表
     *
     * @return
     */
    @GetMapping("page")
    public ResponseEntity page(@CurrentUser UserDetailsImpl userDetails, TownPageDto TownPageDto){
        RespBody body = townService.page(userDetails, TownPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 查询某个镇详细信息
     *
     * @return
     */
    @GetMapping("/details")
    public ResponseEntity details(String uid){
        RespBody body = townService.details(uid);
        return ResponseEntity.ok(body);
    }


    /**
     * 增加镇
     *
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity add(@CurrentUser UserDetailsImpl userDetails, TownAddDto townAddDto){
        RespBody body = townService.add(userDetails, townAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改镇
     *
     * @return
     */
    @PostMapping("/update")
    public ResponseEntity update(@CurrentUser UserDetailsImpl userDetails, TownAddDto townAddDto){
        RespBody body = townService.update(userDetails, townAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除镇
     *
     * @return
     */
    @DeleteMapping("/delete")
    public ResponseEntity delete(String uid){
        RespBody body = townService.delete(uid);
        return ResponseEntity.ok(body);
    }
}
