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

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/mobile/notification")
public class NotificationApi {
    private NotificationService notificationService;

    @Autowired
    public NotificationApi(NotificationService notificationService) {
        this.notificationService = notificationService;
    }



    /**
     * 移动端普通代表收到的通知，分页查询
     * @param userDetails 用户信息
     * @param pageDto 通知页面dto
     * @return
     */
    @GetMapping("/received_page")
    public ResponseEntity mobileReceivedPage(@CurrentUser UserDetailsImpl userDetails,NotificationPageDto pageDto){
        RespBody body = notificationService.mobileReceivedPage(userDetails,pageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 移动端审核人收到的通知，分页查询
     * @param userDetails 用户信息
     * @param pageDto 通知页面dto
     * @return
     */
    @GetMapping("/review_page")
    public ResponseEntity mobileReviewPage(@CurrentUser UserDetailsImpl userDetails,NotificationPageDto pageDto){
        RespBody body = notificationService.mobileReviewPage(userDetails,pageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 接收人获取某一通知的细节
     *
     * @param uid 通知uid
     * @return
     */
    @GetMapping("/details_for_receiver/{uid}")
    public ResponseEntity detailsForMobileReceiver(@CurrentUser UserDetailsImpl userDetails,@PathVariable String uid,Byte level){
        RespBody body = notificationService.detailsForMobileReceiver(userDetails,uid,level);
        return ResponseEntity.ok(body);
    }


    /**
     * 审核人获取某一通知的细节
     *
     * @param uid 通知uid
     * @return
     */
    @GetMapping("/details_for_reviewer/{uid}")
    public ResponseEntity detailsForMobileReviewer(@CurrentUser UserDetailsImpl userDetails,@PathVariable String uid,Byte level){
        RespBody body = notificationService.detailsForMobileReviewer(userDetails,uid,level);
        return ResponseEntity.ok(body);
    }

    /**
     * 审核人对通知进行审核
     * @param userDetails 用户信息
     * @param dto 通知审核参数封装对象
     * @return
     */
    @PostMapping("/review")
    public ResponseEntity review(@CurrentUser UserDetailsImpl userDetails, NotificationReviewDto dto){
        RespBody body = notificationService.review(userDetails,dto);
        return ResponseEntity.ok(body);
    }


    /**
     * 通知审核人 将通知公开
     *
     * @param uid 通知uid
     * @return
     */
    @PutMapping("/publish")
    public ResponseEntity publishForMobile(@CurrentUser UserDetailsImpl userDetails,String uid,Byte level){
        RespBody body = notificationService.publishForMobile(userDetails,uid, level);
        return ResponseEntity.ok(body);
    }

    /**
     * 下载附件
     *
     * @param response
     * @param uid
     */
    @GetMapping("/download_attachment")
    public void downloadAttachment(HttpServletResponse response, @CurrentUser UserDetailsImpl uds, String uid) {
        notificationService.downloadAttachment(response, uds, uid);
    }
}
