package com.cdkhd.npc.controller;
/*
 * @description:政府模块控制器
 * @author:liyang
 * @create:2020-05-20
 */

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.GovAddDto;
import com.cdkhd.npc.service.GovService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/gov")
public class GovernmentController {

    private GovService govService;

    @Autowired
    public GovernmentController(GovService govService) {
        this.govService = govService;
    }

    /**
     * @Description: 添加政府
     * @Param: userDetails govAddDto
     * @Return:
     * @Date: 2020/5/20
     * @Author: LiYang
     */
    @PostMapping("/addGovernment")
    public ResponseEntity addGovernment(@CurrentUser UserDetailsImpl userDetails, GovAddDto govAddDto) {
        RespBody body = govService.addGovernment(userDetails, govAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * @Description: 修改政府
     * @Param: userDetails govAddDto
     * @Return:
     * @Date: 2020/5/20
     * @Author: LiYang
     */
    @PostMapping("/updateGovernment")
    public ResponseEntity updateGovernment(GovAddDto govAddDto) {
        RespBody body = govService.updateGovernment(govAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * @Description: 政府详情
     * @Param: govUid
     * @Return:
     * @Date: 2020/5/20
     * @Author: LiYang
     */
    @GetMapping("/detailGovernment")
    public ResponseEntity detailGovernment(@CurrentUser UserDetailsImpl userDetails){
        RespBody body = govService.detailGovernment(userDetails);
        return ResponseEntity.ok(body);
    }
}
