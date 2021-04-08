package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NewsTypeAddDto;
import com.cdkhd.npc.entity.dto.NewsTypePageDto;
import com.cdkhd.npc.service.NewsTypeService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

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
    public ResponseEntity addNewsType(@CurrentUser UserDetailsImpl userDetails, NewsTypeAddDto addDto){
        RespBody body = newsTypeService.addNewsType(userDetails,addDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 更新新闻类别(栏目)
     * @param dto 待更新的新闻类型信息
     * @return 更新结果
     */
    @PostMapping("/update")
    public ResponseEntity updateNewsType(@CurrentUser UserDetailsImpl userDetails,NewsTypeAddDto dto){
        RespBody body = newsTypeService.updateNewsType(userDetails,dto);
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
    public ResponseEntity pageOfNewsType(@CurrentUser UserDetailsImpl userDetails,NewsTypePageDto pageDto){
        RespBody body = newsTypeService.pageOfNewsType(userDetails,pageDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 调整新闻类别(栏目)顺序
     * @param uid 需调整对象的uid
     * @param direction 移动方向
     * @return 查询结果
     */
    @PostMapping("/change_sequence")
    public ResponseEntity changeSequence(String uid, int direction){
        RespBody body = newsTypeService.changeTypeSequence(uid,direction);
        return ResponseEntity.ok(body);
    }
}
