package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;


public interface NewsService {
    RespBody add(UserDetailsImpl userDetails, NewsAddDto dto);

    RespBody uploadImage(UploadPicDto dto);

    RespBody delete(String uid);

    RespBody update(NewsAddDto dto);

    RespBody publish(BaseDto dto);

    RespBody page(UserDetailsImpl userDetails, NewsPageDto pageDto);

    RespBody pageForMobile(NewsPageDto pageDto);

    RespBody details(String uid);

    RespBody review(MobileUserDetailsImpl userDetails, NewsReviewDto dto);

    RespBody toReview(UserDetailsImpl userDetails,String uid);

    RespBody setPriority(NewsWhereShowDto dto);
}
