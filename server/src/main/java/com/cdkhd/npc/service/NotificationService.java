package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;

import com.cdkhd.npc.vo.RespBody;

public interface NotificationService {

    RespBody add(UserDetailsImpl userDetails, NotificationAddDto dto);

    RespBody uploadAttachment(AttachmentDto dto);

    RespBody delete(String uid);

    RespBody update(NotificationAddDto dto);

    RespBody publish(String uid);

    RespBody page(UserDetailsImpl userDetails, NotificationPageDto pageDto);

    RespBody details(String uid);

    RespBody review(UserDetailsImpl userDetails, NotificationReviewDto dto);

    RespBody toReview(UserDetailsImpl userDetails,String uid);

}