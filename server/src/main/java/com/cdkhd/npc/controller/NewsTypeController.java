package com.cdkhd.npc.controller;

import com.cdkhd.npc.entity.dto.NewsTypeAddDto;
import com.cdkhd.npc.entity.dto.NewsTypePageDto;
import com.cdkhd.npc.service.NewsTypeService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/manager/news_type")
public class NewsTypeController {
    private NewsTypeService newsTypeService;

    @Autowired
    public NewsTypeController(NewsTypeService newsTypeService) {
        this.newsTypeService = newsTypeService;
    }

    /**
     * 添加新闻类别(栏目)
     * @param addDto 待添加的新闻类型信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity addNewsType(NewsTypeAddDto addDto){
        RespBody body = newsTypeService.addNewsType(addDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 更新新闻类别(栏目)
     * @param dto 待更新的新闻类型信息
     * @return 更新结果
     */
    @PutMapping
    public ResponseEntity updateNewsType(NewsTypeAddDto dto){
        RespBody body = newsTypeService.updateNewsType(dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除新闻类别(栏目)
     * @param uid 待删除的新闻类别的uid
     * @return 删除结果
     */
    @DeleteMapping("/{uid}")
    public ResponseEntity deleteNewsType(@PathVariable String uid){
        RespBody body = newsTypeService.deleteNewsType(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 分页查询新闻类别(栏目)
     * @param pageDto 查询条件
     * @return 查询结果
     */
    @GetMapping
    public ResponseEntity pageOfNewsType(NewsTypePageDto pageDto){
        RespBody body = newsTypeService.pageOfNewsType(pageDto);
        return ResponseEntity.ok(body);
    }

}
