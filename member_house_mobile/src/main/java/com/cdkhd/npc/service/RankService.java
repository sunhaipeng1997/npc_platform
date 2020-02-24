package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface RankService {

    /**
     * 代表建议排名
     */
    RespBody memberSuggestionRank(UserDetailsImpl userDetails, Byte level);

    /**
     * 各镇建议排名
     */
    RespBody townSuggestionRank(UserDetailsImpl userDetails, Byte level);

    /**
     * 代表受到的意见排名
     */
    RespBody memberOpinionRank(UserDetailsImpl userDetails, Byte level);

    /**
     * 各镇收到的意见排名
     */
    RespBody townOpinionRank(UserDetailsImpl userDetails, Byte level);

    /**
     * 代表履职排名
     */
    RespBody memberPerformanceRank(UserDetailsImpl userDetails, Byte level);

    /**
     * 各镇履职排名
     */
    RespBody townPerformanceRank(UserDetailsImpl userDetails, Byte level);
}
