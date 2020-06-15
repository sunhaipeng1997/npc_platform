package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.service.IndexService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/index")
public class IndexController {

    private IndexService indexService;

    @Autowired
    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    /**
     * 政府首页本月数量显示
     */
    @GetMapping("/getGovSugNumber")
    public ResponseEntity getGovSugNumber(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.getGovSugNumber(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 政府首页建议数量趋势图
     */
    @GetMapping("/getGovSugCount")
    public ResponseEntity getGovSugCount(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.getGovSugCount(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 政府首页建议按类型统计图
     */
    @GetMapping("/sugGovBusinessLine")
    public ResponseEntity sugGovBusinessLine(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.sugGovBusinessLine(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 政府首页建议办理中的建议统计图
     */
    @GetMapping("/sugGovUnitDealingLine")
    public ResponseEntity sugGovUnitDealingLine(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.sugGovUnitDealingLine(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 政府首页建议办结的建议统计图
     */
    @GetMapping("/sugGovUnitCompletedLine")
    public ResponseEntity sugGovUnitCompletedLine(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.sugGovUnitCompletedLine(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 后台管理员查看首页本月新增建议数量
    * @Param: userDetails
    * @Return:
    * @Date: 2020/6/11
    * @Author: LiYang
    */
    @GetMapping("/adminGetSugNumber")
    public ResponseEntity adminGetSugNumber(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.adminGetSugNumber(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 人大后台管理员首页建议数量趋势图
    * @Param:
    * @Return:
    * @Date: 2020/6/11
    * @Author: LiYang
    */
    @GetMapping("/adminNewSugNum")
    public ResponseEntity adminNewSugNum(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.adminNewSugNum(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 人大后台管理员首页建议按类型统计图
    * @Param:
    * @Return:
    * @Date: 2020/6/11
    * @Author: LiYang
    */
    @GetMapping("/adminSugBusinessLine")
    public ResponseEntity adminSugBusinessLine(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.adminSugBusinessLine(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
    * @Description: 人大后台管理员首页建议按下属机构分组统计新增建议
    * @Param:
    * @Return:
    * @Date: 2020/6/12
    * @Author: LiYang
    */
    @GetMapping("/adminSugNumGroupBySubordinate")
    public ResponseEntity adminSugNumGroupBySubordinate(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = indexService.adminSugNumGroupBySubordinate(userDetails);
        return ResponseEntity.ok(body);
    }
}
