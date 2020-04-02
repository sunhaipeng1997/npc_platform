package com.cdkhd.npc.api;


import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.NewsPageDto;
import com.cdkhd.npc.entity.dto.NewsReviewDto;
import com.cdkhd.npc.entity.dto.NewsTypePageDto;
import com.cdkhd.npc.service.NewsService;
import com.cdkhd.npc.service.NewsTypeService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/mobile/news")
public class NewsApi {
    private NewsService newsService;
    private NewsTypeService newsTypeService;

    @Autowired
    public NewsApi(NewsService newsService, NewsTypeService newsTypeService) {
        this.newsService = newsService;
        this.newsTypeService = newsTypeService;
    }

    /**
     * 分页查询
     * @param
     * @param pageDto 新闻页面dto
     * @return
     */
    @GetMapping
    public ResponseEntity page(NewsPageDto pageDto){
        RespBody body = newsService.pageForMobile(pageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 移动端审核人收到的通知，分页查询
     * @param userDetails 用户信息
     * @param pageDto 通知页面dto
     * @return
     */
    @GetMapping("/review_page")
    public ResponseEntity mobileReviewPage(@CurrentUser MobileUserDetailsImpl userDetails,NewsPageDto pageDto){
        RespBody body = newsService.mobileReviewPage(userDetails,pageDto);
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
     * 审核人获取某一通知的细节
     *
     * @param uid 通知uid
     * @return
     */
    @GetMapping("/details_for_reviewer/{uid}")
    public ResponseEntity detailsForMobileReviewer(@CurrentUser MobileUserDetailsImpl userDetails,@PathVariable String uid,Byte level){
        RespBody body = newsService.detailsForMobileReviewer(userDetails,uid,level);
        return ResponseEntity.ok(body);
    }


    /**
     * 审核人对新闻进行审核
     * @param userDetails 用户信息
     * @param dto 新闻审核参数封装对象
     * @return
     */
    @PostMapping("/review")
    public ResponseEntity review(@CurrentUser MobileUserDetailsImpl userDetails, NewsReviewDto dto){
        RespBody body = newsService.review(userDetails,dto);
        return ResponseEntity.ok(body);
    }


    /**
     * 后台管理员 或者 新闻审核人 将新闻公开
     *
     * @param dto 新闻uid
     * @return
     */
    @PostMapping("/publish")
    public ResponseEntity publish(BaseDto dto){
        RespBody body = newsService.publish(dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 分页查询新闻类别(栏目)
     * @param pageDto 查询条件
     * @return 查询结果
     */
    @GetMapping("/type")
    public ResponseEntity pageOfNewsType(NewsTypePageDto pageDto){
        RespBody body = newsTypeService.pageOfNewsTypeForMobile(pageDto);
        return ResponseEntity.ok(body);
    }
}
