package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface StudyService {


    /**
     * 学习资料返回
     * @param userDetails
     * @return
     */
    RespBody studiesList(UserDetailsImpl userDetails);



}
