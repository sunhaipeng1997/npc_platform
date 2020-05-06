package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.TypeDto;
import com.cdkhd.npc.vo.RespBody;

public interface RankService {

    /**
     * 代表建议排名
     */
    RespBody memberSuggestionRank(MobileUserDetailsImpl userDetails, Byte level);

    /**
     * 各镇建议排名
     */
    RespBody townSuggestionRank(MobileUserDetailsImpl userDetails, TypeDto typeDto);

    /**
     * 代表受到的意见排名
     */
    RespBody memberOpinionRank(MobileUserDetailsImpl userDetails, Byte level);

    /**
     * 各镇收到的意见排名
     */
    RespBody townOpinionRank(MobileUserDetailsImpl userDetails, TypeDto typeDto);

    /**
     * 代表履职排名
     */
    RespBody memberPerformanceRank(MobileUserDetailsImpl userDetails, Byte level);

    /**
     * 各镇履职排名
     */
    RespBody townPerformanceRank(MobileUserDetailsImpl userDetails, TypeDto typeDto);
}
