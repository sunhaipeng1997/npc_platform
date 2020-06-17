package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.HandleProcessAddDto;
import com.cdkhd.npc.entity.dto.InDealingPageDto;
import com.cdkhd.npc.entity.dto.ResultAddDto;
import com.cdkhd.npc.entity.dto.ToDealPageDto;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface UnitSuggestionService {

    RespBody findToDeal(UserDetailsImpl userDetails, ToDealPageDto pageDto);

    RespBody applyAdjust(UserDetailsImpl userDetails, String conveyProcessUid, String adjustReason);

    RespBody startDealing(UserDetailsImpl userDetails, String conveyProcessUid);

    RespBody findPageOfInDealing(UserDetailsImpl userDetails, InDealingPageDto pageDto);

    RespBody applyDelay(UserDetailsImpl userDetails, String unitSuggestionUid, Date delayUntil, String reason);

    RespBody addHandleProcess(UserDetailsImpl userDetails, HandleProcessAddDto toAdd);

    RespBody finishDeal(UserDetailsImpl userDetails, ResultAddDto toAdd);

    RespBody uploadOneImage(UserDetailsImpl userDetails, MultipartFile image, Byte type);
}
