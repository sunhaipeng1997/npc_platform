package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.vo.RespBody;

public interface UnitSuggestionService {
    RespBody findPageOfToDeal(MobileUserDetailsImpl userDetails, PageDto pageDto);

    RespBody checkToDealDetail(MobileUserDetailsImpl userDetails, String conveyProcessUid);
}
