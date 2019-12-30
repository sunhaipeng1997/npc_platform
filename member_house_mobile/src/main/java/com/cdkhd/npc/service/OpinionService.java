package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AddOpinionDto;
import com.cdkhd.npc.entity.dto.OpinionDto;
import com.cdkhd.npc.vo.RespBody;

public interface OpinionService {


    /**
     * 意见管理
     * @param userDetails
     * @return
     */
    RespBody addOpinion(UserDetailsImpl userDetails, AddOpinionDto addOpinionDto);

    /**
     * 我的意见列表
     * @param userDetails
     * @return
     */
    RespBody myOpinions(UserDetailsImpl userDetails, OpinionDto opinionDto);


}
