package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NotificationPageDto;
import com.cdkhd.npc.entity.dto.NotificationReviewDto;
import com.cdkhd.npc.service.NotificationService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/mobile/notification")
public class NotificationApi {
    private NotificationService notificationService;

    @Autowired
    public NotificationApi(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    /**
     * 分页查询
     *   用户信息（为了方便测试，暂时不加）
     * @param pageDto 通知页面dto
     * @return
     */
    @GetMapping
    public ResponseEntity page(NotificationPageDto pageDto){
        RespBody body = notificationService.pageForMobileTest(pageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取某一通知的细节
     *
     * @param uid 通知uid
     * @return
     */
    @GetMapping("/{uid}")
    public ResponseEntity details(String userName,@PathVariable String uid){
        RespBody body = notificationService.detailsForMobileTest(userName,uid);
        return ResponseEntity.ok(body);
    }


//    /**
//     * 审核人对通知进行审核
//     * @param userDetails 用户信息
//     * @param dto 通知审核参数封装对象
//     * @return
//     */
//    @PostMapping("/review")
//    public ResponseEntity review(@CurrentUser UserDetailsImpl userDetails, NotificationReviewDto dto){
//        RespBody body = notificationService.review(userDetails,dto);
//        return ResponseEntity.ok(body);
//    }

    //测试
    @PostMapping("/review")
    public ResponseEntity review(NotificationReviewDto dto){
        RespBody body = notificationService.reviewForMobileTest(dto);
        return ResponseEntity.ok(body);
    }


    /**
     * 后台管理员 或者 通知审核人 将通知公开
     *
     * @param uid 通知uid
     * @return
     */
    @PutMapping("/publish")
    public ResponseEntity publish(String userName,String uid){//测试
        RespBody body = notificationService.publishForMobileTest(userName,uid);
        return ResponseEntity.ok(body);
    }
}
