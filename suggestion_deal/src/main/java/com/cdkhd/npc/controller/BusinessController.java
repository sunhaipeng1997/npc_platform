package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.UnitAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitPageDto;
import com.cdkhd.npc.entity.dto.UnitUserAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitUserPageDto;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.UnitService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/business")
public class BusinessController {

    private GeneralService generalService;

    @Autowired
    public BusinessController(GeneralService generalService) {
        this.generalService = generalService;
    }

    /**
     * 分页查询单位信息
     */
    @GetMapping("/findSugBusiness")
    public ResponseEntity findSugBusiness(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = generalService.findSugBusiness(userDetails);
        return ResponseEntity.ok(body);
    }

}
