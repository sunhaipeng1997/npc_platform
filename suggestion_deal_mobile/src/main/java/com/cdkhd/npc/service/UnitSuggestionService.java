package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.dto.HandleProcessAddDto;
import com.cdkhd.npc.entity.dto.ResultAddDto;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public interface UnitSuggestionService {
    RespBody findPageOfToDeal(MobileUserDetailsImpl userDetails, PageDto pageDto);

    RespBody checkToDealDetail(MobileUserDetailsImpl userDetails, String conveyProcessUid);

    RespBody applyAdjust(MobileUserDetailsImpl userDetails, String conveyProcessUid, String adjustReason);

    RespBody startDealing(MobileUserDetailsImpl userDetails, String conveyProcessUid);

    RespBody findPageOfInDealing(MobileUserDetailsImpl userDetails, PageDto pageDto);

    RespBody checkDealingDetail(MobileUserDetailsImpl userDetails, String unitSuggestionUid);

    RespBody applyDelay(MobileUserDetailsImpl userDetails, String unitSuggestionUid, Date delayUntil, String reason);

    RespBody addHandleProcess(MobileUserDetailsImpl userDetails, HandleProcessAddDto toAdd);

    RespBody finishDeal(MobileUserDetailsImpl userDetails, ResultAddDto toAdd);

    RespBody uploadOneImage(MobileUserDetailsImpl userDetails, MultipartFile image, Byte type);

    RespBody findPageOfDone(MobileUserDetailsImpl userDetails, PageDto pageDto);
}
