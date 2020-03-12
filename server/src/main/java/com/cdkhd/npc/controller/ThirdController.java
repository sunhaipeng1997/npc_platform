package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.service.ThirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/server/third")
public class ThirdController {

    private  ThirdService thirdService;

    @Autowired
    public ThirdController(ThirdService thirdService) {
        this.thirdService = thirdService;
    }

    /**
     * 查询镇列表
     *
     * @return
     */
    @GetMapping("/getRelation")
    public AreaVo getRelation(@CurrentUser UserDetailsImpl userDetails){
        return thirdService.getRelation(userDetails);
    }

}
