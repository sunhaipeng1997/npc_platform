package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.vo.RespBody;

public interface ThirdService {

    AreaVo getRelation(UserDetailsImpl userDetails);

    RespBody countSuggestions4Town(UserDetailsImpl userDetails);

    RespBody countOpinions4Town(UserDetailsImpl userDetails);

    RespBody countPerformances4Town(UserDetailsImpl userDetails);

    RespBody countSuggestions4Type(UserDetailsImpl userDetails);

    RespBody countEducation4NpcMember(UserDetailsImpl userDetails);

    RespBody countAll(UserDetailsImpl userDetails);
}
