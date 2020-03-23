package com.cdkhd.npc.controller;


import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AttachmentDto;
import com.cdkhd.npc.entity.dto.NotificationAddDto;
import com.cdkhd.npc.entity.dto.NotificationPageDto;
import com.cdkhd.npc.entity.dto.UploadPicDto;
import com.cdkhd.npc.service.NotificationService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/manager/notification")
public class NotificationController {
    private NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity add(@CurrentUser UserDetailsImpl userDetails, NotificationAddDto dto){
        RespBody body = notificationService.add(userDetails,dto);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/upload_attachment")
    public ResponseEntity uploadAttachment(AttachmentDto dto) {
        RespBody body = notificationService.uploadAttachment(dto);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity delete(@PathVariable String uid) {
        RespBody body = notificationService.delete(uid);
        return ResponseEntity.ok(body);
    }

    @PutMapping
    public ResponseEntity update(UserDetailsImpl userDetails,NotificationAddDto dto) {
        RespBody body = notificationService.update(userDetails,dto);
        return ResponseEntity.ok(body);
    }

    /**
     * 分页查询
     * @param userDetails 用户信息
     * @param pageDto 通知页面dto
     * @return
     */
    @GetMapping
    public ResponseEntity page(@CurrentUser UserDetailsImpl userDetails, NotificationPageDto pageDto){
        RespBody body = notificationService.page(userDetails,pageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取某一通知的细节
     *
     * @param uid 通知uid
     * @return
     */
    @GetMapping("/{uid}")
    public ResponseEntity details(@PathVariable String uid){
        RespBody body = notificationService.details(uid);
        return ResponseEntity.ok(body);
    }


    /**
     * 后台管理员提交通知审核
     *
     * @param userDetails 用户信息
     * @param uid   通知uid
     * @return
     */
    @PostMapping("/to_review/{uid}")
    public ResponseEntity toReview(@CurrentUser UserDetailsImpl userDetails,@PathVariable String uid){
        RespBody body = notificationService.toReview(userDetails,uid);
        return ResponseEntity.ok(body);
    }

}