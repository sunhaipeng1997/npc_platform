package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;

import com.cdkhd.npc.vo.RespBody;

import javax.servlet.http.HttpServletResponse;

public interface NotificationService {

    //后台服务
    RespBody uploadAttachment(AttachmentDto dto);
    RespBody add(UserDetailsImpl userDetails, NotificationAddDto dto);
    RespBody delete(String uid);
    RespBody update(UserDetailsImpl userDetails,NotificationAddDto dto);
    RespBody toReview(UserDetailsImpl userDetails,String uid);
    RespBody publish(UserDetailsImpl userDetails,String uid);
    RespBody page(UserDetailsImpl userDetails, NotificationPageDto pageDto);
    RespBody details(String uid);//pc后台前端获取细节

    //移动端
    RespBody mobileReceivedPage(UserDetailsImpl userDetails, NotificationPageDto pageDto);//代表收到的通知列表
    RespBody mobileReviewPage(UserDetailsImpl userDetails, NotificationPageDto pageDto);//审核人收到的通知列表
    RespBody detailsForMobileReceiver(UserDetailsImpl userDetails,String uid,Byte level);//移动端端通知接收人获取通知详情
    RespBody detailsForMobileReviewer(UserDetailsImpl userDetails,String uid,Byte level);//移动端端通知审核人获取通知详情、及其审核记录
    RespBody review(UserDetailsImpl userDetails, NotificationReviewDto dto);
    RespBody publishForMobile(UserDetailsImpl userDetails,String uid,Byte level);

    void downloadAttachment(HttpServletResponse response, UserDetailsImpl uds, String uid);
}
