package com.cdkhd.npc.service;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;


public interface NewsService {

    //pc
    RespBody uploadImage(UploadPicDto dto);
    RespBody add(UserDetailsImpl userDetails, NewsAddDto dto);
    RespBody delete(String uid);
    RespBody update(NewsAddDto dto);
    RespBody publish(BaseDto dto);
    RespBody page(UserDetailsImpl userDetails, NewsPageDto pageDto);
    RespBody details(String uid);//pc及移动端普通用户
    RespBody toReview(UserDetailsImpl userDetails,String uid);
    RespBody setPriority(NewsWhereShowDto dto);

    //移动端
    RespBody pageForMobile(NewsPageDto pageDto);
    RespBody mobileReviewPage(MobileUserDetailsImpl userDetails, NewsPageDto pageDto);
    RespBody review(MobileUserDetailsImpl userDetails, NewsReviewDto dto);
    //移动端端通知审核人获取新闻详情、及其审核记录
    RespBody detailsForMobileReviewer(MobileUserDetailsImpl userDetails,String uid,Byte level);

}
