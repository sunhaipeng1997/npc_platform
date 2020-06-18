package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.vo.RespBody;

public interface ThirdService {

    AreaVo getRelation(UserDetailsImpl userDetails);

    RespBody countSuggestions4Town(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countOpinions4Town(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countPerformances4Town(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countSuggestions4Type(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countEducation4NpcMember(UserDetailsImpl userDetails, Byte level, String uid);

    RespBody countAll(UserDetailsImpl userDetails, Byte level, String uid);
}
