package com.cdkhd.npc.controller;

import com.cdkhd.npc.service.CommonDictService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/commonDict")
public class CommonDictController {

    private CommonDictService commonDictService;

    @Autowired
    public CommonDictController(CommonDictService commonDictService) {
        this.commonDictService = commonDictService;
    }

    /**
     * 获取民族信息
     * @return 查询结果
     */
    @GetMapping("/getListByKey")
    public ResponseEntity getListByKey(String key) {
        RespBody body = commonDictService.getListByKey(key);
        return ResponseEntity.ok(body);
    }

}
