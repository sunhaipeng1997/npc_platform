package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.LevelDto;
import com.cdkhd.npc.service.UnitService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal_mobile/unit")
public class UnitApi {

    private UnitService unitService;

    @Autowired
    public UnitApi(UnitService unitService) {
        this.unitService = unitService;
    }


    /**
     * 获取单位下拉列表
     */
    @GetMapping("/unitList")
    public ResponseEntity unitList(@CurrentUser MobileUserDetailsImpl userDetails, LevelDto levelDto) {
        RespBody body = unitService.unitList(userDetails, levelDto);
        return ResponseEntity.ok(body);
    }

}
