package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.VillageAddDto;
import com.cdkhd.npc.entity.dto.VillagePageDto;
import com.cdkhd.npc.service.VillageService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/manager/village")
public class VillageController {

    private final VillageService villageService;

    @Autowired
    public VillageController(VillageService villageService) {
        this.villageService = villageService;
    }

    /**
     * 查询村相关信息
     *
     * @return
     */
    @GetMapping("/findVillage")
    public ResponseEntity findVillage(@CurrentUser UserDetailsImpl userDetails, VillagePageDto villagePageDto) {
        RespBody body = villageService.findVillage(userDetails, villagePageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加、修改村
     *
     * @return
     */
    @PostMapping("/addOrUpdateVillage")
    public ResponseEntity addOrUpdateVillage(@CurrentUser UserDetailsImpl userDetails, VillageAddDto villageAddDto) {
        RespBody body = villageService.addOrUpdateVillage(userDetails, villageAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除村
     *
     * @return
     */
    @DeleteMapping("/deleteVillage")
    public ResponseEntity deleteVillage(String uid) {
        RespBody body = villageService.deleteVillage(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 查询没有被小组包含的村
     *
     * @return
     */
    @GetMapping("/optional")
    public ResponseEntity optional(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = villageService.optional(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 查询小组可以操作的村
     *
     * @return
     */
    @GetMapping("/modifiable")
    public ResponseEntity modifiable(@CurrentUser UserDetailsImpl userDetails, String uid) {
        RespBody body = villageService.modifiable(userDetails, uid);
        return ResponseEntity.ok(body);
    }
}
