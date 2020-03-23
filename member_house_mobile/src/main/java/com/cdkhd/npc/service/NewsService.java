package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NewsPageDto;
import com.cdkhd.npc.entity.dto.NewsReviewDto;
import com.cdkhd.npc.vo.RespBody;


public interface NewsService {

    RespBody publish(String uid);

    RespBody page(UserDetailsImpl userDetails, NewsPageDto pageDto);

    RespBody details(String uid);

    RespBody review(UserDetailsImpl userDetails, NewsReviewDto dto);
}
