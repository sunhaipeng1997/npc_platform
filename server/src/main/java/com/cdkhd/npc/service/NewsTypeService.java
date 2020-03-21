package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NewsTypeAddDto;
import com.cdkhd.npc.entity.dto.NewsTypePageDto;
import com.cdkhd.npc.vo.RespBody;

import javax.validation.constraints.NotBlank;

public interface NewsTypeService {
    RespBody pageOfNewsType(UserDetailsImpl userDetails,NewsTypePageDto pageDto);

    RespBody pageOfNewsTypeForMobile(NewsTypePageDto pageDto);

    RespBody addNewsType(UserDetailsImpl userDetails,NewsTypeAddDto addDto);

    RespBody updateNewsType(UserDetailsImpl userDetails,NewsTypeAddDto dto);

    RespBody deleteNewsType(@NotBlank String uid);

    RespBody changeTypeSequence(@NotBlank String uid, int direction);
}
