package com.cdkhd.npc.controller;


import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.NewsAddDto;
import com.cdkhd.npc.entity.dto.NewsPageDto;
import com.cdkhd.npc.entity.dto.NewsWhereShowDto;
import com.cdkhd.npc.entity.dto.UploadPicDto;
import com.cdkhd.npc.service.NewsService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/manager/news")
public class NewsController {
    private NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    public ResponseEntity add(@CurrentUser UserDetailsImpl userDetails, NewsAddDto dto){
        RespBody body = newsService.add(userDetails,dto);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/upload_image")
    public ResponseEntity uploadImage(UploadPicDto dto) {
        RespBody body = newsService.uploadImage(dto);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity delete(@PathVariable String uid) {
        RespBody body = newsService.delete(uid);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/update")
    public ResponseEntity update(NewsAddDto dto) {
        RespBody body = newsService.update(dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 分页查询
     * @param userDetails 用户信息
     * @param pageDto 新闻页面dto
     * @return
     */
    @GetMapping
    public ResponseEntity page(@CurrentUser UserDetailsImpl userDetails, NewsPageDto pageDto){
        RespBody body = newsService.page(userDetails,pageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取某一新闻的细节
     *
     * @param uid 新闻uid
     * @return
     */
    @GetMapping("/{uid}")
    public ResponseEntity details(@PathVariable String uid){
        RespBody body = newsService.details(uid);
        return ResponseEntity.ok(body);
    }


    /**
     * 后台管理员提交新闻审核
     *
     * @param userDetails 用户信息
     * @param uid   新闻uid
     * @return
     */
    @PostMapping("/to_review/{uid}")
    public ResponseEntity toReview(@CurrentUser UserDetailsImpl userDetails,@PathVariable String uid){
        RespBody body = newsService.toReview(userDetails,uid);
        return ResponseEntity.ok(body);
    }

    /**
     *
     * @param dto   新闻uid
     * @return
     */
    @PostMapping("/priority")
    public ResponseEntity setPriority(NewsWhereShowDto dto){
        RespBody body = newsService.setPriority(dto);
        return ResponseEntity.ok(body);
    }


    /**
     * 后台管理员将新闻公开
     *
     * @return
     */
    @PostMapping("/publish")
    public ResponseEntity publish(@CurrentUser UserDetailsImpl userDetails, BaseDto dto){
        RespBody body = newsService.publish(userDetails,dto);
        return ResponseEntity.ok(body);
    }


}
