package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.vo.RespBody;

public interface GovSuggestionService {

    /**
     * 全部建议业务类型下拉列表
     * */
    RespBody getGovSuggestion(UserDetailsImpl userDetails, SuggestionDto suggestionDto);

}
