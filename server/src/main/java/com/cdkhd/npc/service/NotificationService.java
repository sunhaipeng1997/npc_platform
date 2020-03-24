package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;

import com.cdkhd.npc.vo.RespBody;

public interface NotificationService {

    RespBody add(UserDetailsImpl userDetails, NotificationAddDto dto);

    RespBody uploadAttachment(AttachmentDto dto);

    RespBody delete(String uid);

    RespBody update(UserDetailsImpl userDetails,NotificationAddDto dto);

    RespBody publish(UserDetailsImpl userDetails,String uid);

    //临时这样写，因为小程序的登录还没写好,所以暂时不要userDetails
    RespBody publishForMobileTest(String userName,String uid);

    RespBody page(UserDetailsImpl userDetails, NotificationPageDto pageDto);

    //临时这样写，因为小程序的登录还没写好,所以暂时不要userDetails
    RespBody pageForMobileTest(NotificationPageDto pageDto);

    RespBody details(String uid);//pc
    RespBody detailsForMobile(UserDetailsImpl userDetails,String uid);//小程序
    RespBody detailsForMobileTest(String userName,String uid);//测试

    RespBody review(UserDetailsImpl userDetails, NotificationReviewDto dto);
    RespBody reviewForMobileTest(NotificationReviewDto dto);//测试

    RespBody toReview(UserDetailsImpl userDetails,String uid);
}
