package com.cdkhd.npc.api;


import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NewsPageDto;
import com.cdkhd.npc.entity.dto.NewsReviewDto;
import com.cdkhd.npc.service.NewsService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/mobile/news")
public class NewsApi {
    private NewsService newsService;

    @Autowired
    public NewsApi(NewsService newsService) {
        this.newsService = newsService;
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
     * 审核人对新闻进行审核
     * @param userDetails 用户信息
     * @param dto 新闻审核参数封装对象
     * @return
     */
    @PostMapping("/review")
    public ResponseEntity review(@CurrentUser UserDetailsImpl userDetails, NewsReviewDto dto){
        RespBody body = newsService.review(userDetails,dto);
        return ResponseEntity.ok(body);
    }


    /**
     * 后台管理员 或者 新闻审核人 将新闻公开
     *
     * @param uid 新闻uid
     * @return
     */
    @PutMapping("/publish")
    public ResponseEntity publish(String uid){
        RespBody body = newsService.publish(uid);
        return ResponseEntity.ok(body);
    }
}