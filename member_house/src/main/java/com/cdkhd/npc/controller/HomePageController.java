package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.service.HomePageService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house/homepage")
public class HomePageController {

    private HomePageService homePageService;

    @Autowired
    public HomePageController(HomePageService homePageService) {
        this.homePageService = homePageService;
    }


    /**
     * 获取今日新增数量
     * @return
     */
    @GetMapping("/getTodayNumber")
    public ResponseEntity getTodayNumber(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = homePageService.getTodayNumber(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 代表建议曲线图
     * @return
     */
    @GetMapping("/drawSuggestion")
    public ResponseEntity drawSuggestion(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = homePageService.drawSuggestion(userDetails);
        return ResponseEntity.ok(body);
    }


    /**
     * 代表收到的意见曲线图
     * @return
     */
    @GetMapping("/drawOpinion")
    public ResponseEntity drawOpinion(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = homePageService.drawOpinion(userDetails);
        return ResponseEntity.ok(body);
    }


    /**
     * 代表履职曲线图
     * @return
     */
    @GetMapping("/drawPerformance")
    public ResponseEntity drawPerformance(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = homePageService.drawPerformance(userDetails);
        return ResponseEntity.ok(body);
    }


    /**
     * 代表履职类型数量柱状图
     * @return
     */
    @GetMapping("/drawPerformanceType")
    public ResponseEntity drawPerformanceType(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = homePageService.drawPerformanceType(userDetails);
        return ResponseEntity.ok(body);
    }



}
