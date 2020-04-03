package com.cdkhd.npc.service;

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
    RespBody publish(UserDetailsImpl userDetails, BaseDto dto);
    RespBody page(UserDetailsImpl userDetails, NewsPageDto pageDto);
    RespBody details(String uid);//pc及移动端普通用户
    RespBody toReview(UserDetailsImpl userDetails,String uid);
    RespBody setPriority(NewsWhereShowDto dto);

    //移动端
    RespBody pageForMobile(NewsPageDto pageDto);////一般用户的列表，按照地区、类别等查询
    RespBody mobileReviewPage(UserDetailsImpl userDetails, NewsPageDto pageDto);//    //审核人的列表
    RespBody review(UserDetailsImpl userDetails, NewsReviewDto dto);
    RespBody publishForMobile(UserDetailsImpl userDetails,String uid,Byte level);
    RespBody detailsForMobileReviewer(UserDetailsImpl userDetails,String uid,Byte level);////移动端端通知审核人获取新闻详情、及其审核记录
}
