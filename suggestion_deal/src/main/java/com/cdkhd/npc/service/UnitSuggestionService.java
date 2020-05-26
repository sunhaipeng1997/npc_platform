package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.GovSuggestionPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface UnitSuggestionService {

    RespBody findToDeal(UserDetailsImpl userDetails, GovSuggestionPageDto dto);

}
