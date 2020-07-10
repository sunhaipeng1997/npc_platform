package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionBusinessAddDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessDto;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface NpcSuggestionService {

    RespBody addOrUpdateSuggestionBusiness(UserDetailsImpl userDetails, SuggestionBusinessAddDto suggestionBusinessAddDto);

    RespBody findSuggestionBusiness(UserDetailsImpl userDetails, SuggestionBusinessDto suggestionBusinessDto);

    RespBody deleteSuggestionBusiness(String uid);

    RespBody changeTypeSequence(UserDetailsImpl userDetails, String uid, Byte type);

    RespBody changeBusinessStatus(String uid, Byte status);

    RespBody findSuggestion(UserDetailsImpl userDetails, SuggestionDto suggestionDto);

    void exportSuggestion(UserDetailsImpl userDetails, SuggestionDto suggestionDto, HttpServletRequest req, HttpServletResponse res);

    RespBody sugBusList(UserDetailsImpl userDetails);
}
