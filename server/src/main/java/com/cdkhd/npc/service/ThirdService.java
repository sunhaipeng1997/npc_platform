package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AddOpinionDto;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.entity.dto.AddSuggestionDto;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.vo.RespBody;

import java.util.List;

public interface ThirdService {

    AreaVo getRelation(UserDetailsImpl userDetails);

    RespBody countSuggestions4Town(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countOpinions4Town(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countPerformances4Town(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countSuggestions4Type(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countEducation4NpcMember(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countAll(UserDetailsImpl userDetails, Byte level, String uid);

    //同步黄龙溪的意见
    RespBody syncOpinion(List<AddOpinionDto> addOpinionDtos);

    //同步黄龙溪的建议
    RespBody syncSuggestion(List<AddSuggestionDto> dtos);

    //同步黄龙溪的履职
    RespBody syncPerformance(List<AddPerformanceDto> dtos);
}
