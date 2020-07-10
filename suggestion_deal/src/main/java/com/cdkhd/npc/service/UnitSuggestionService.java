package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface UnitSuggestionService {

    RespBody findPageOfToDeal(UserDetailsImpl userDetails, ToDealPageDto pageDto);

    RespBody checkToDealDetail(UserDetailsImpl userDetails, String cpUid);

    RespBody applyAdjust(UserDetailsImpl userDetails, String conveyProcessUid, String adjustReason);

    RespBody startDealing(UserDetailsImpl userDetails, String conveyProcessUid);

    RespBody findPageOfInDealing(UserDetailsImpl userDetails, InDealingPageDto pageDto);

    RespBody checkDetail(UserDetailsImpl userDetails, String usUid);

    RespBody applyDelay(UserDetailsImpl userDetails, String unitSuggestionUid, Date delayUntil, String reason);

    RespBody addHandleProcess(UserDetailsImpl userDetails, HandleProcessAddDto toAdd);

    RespBody finishDeal(UserDetailsImpl userDetails, ResultAddDto toAdd);

    RespBody uploadOneImage(UserDetailsImpl userDetails, MultipartFile image, Byte type);

    RespBody findPageOfDone(UserDetailsImpl userDetails, DonePageDto pageDto);

    RespBody findPageOfComplete(UserDetailsImpl userDetails, CompletePageDto pageDto);
}
