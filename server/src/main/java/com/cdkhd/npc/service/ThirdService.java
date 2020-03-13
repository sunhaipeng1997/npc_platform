package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.vo.AreaVo;

public interface ThirdService {

    AreaVo getRelation(UserDetailsImpl userDetails);

}
