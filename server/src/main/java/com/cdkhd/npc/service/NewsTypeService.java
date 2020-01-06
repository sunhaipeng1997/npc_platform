package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NewsTypeAddDto;
import com.cdkhd.npc.entity.dto.NewsTypePageDto;
import com.cdkhd.npc.vo.RespBody;

public interface NewsTypeService {
    RespBody pageOfNewsType(NewsTypePageDto pageDto);

    RespBody addNewsType(NewsTypeAddDto addDto);

    RespBody updateNewsType(NewsTypeAddDto dto);

    RespBody deleteNewsType(String uid);
}
