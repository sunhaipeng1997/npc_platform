package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;

public interface OpinionService {

    //选民意见部分

    /**
     * 意见管理
     * @param userDetails
     * @return
     */
    RespBody addOpinion(MobileUserDetailsImpl userDetails, AddOpinionDto addOpinionDto);

    /**
     * 我的意见列表
     * @param userDetails
     * @return
     */
    RespBody myOpinions(MobileUserDetailsImpl userDetails, OpinionDto opinionDto);

    /**
     * 意见详情
     * @return
     */
    RespBody detailOpinion(OpinionDetailDto opinionDetailDto);

    //代表意见部分

    /**
     * 我收到的意见
     * @param userDetails
     * @param opinionDto
     * @return
     */
    RespBody receiveOpinions(MobileUserDetailsImpl userDetails, OpinionDto opinionDto);

    /**
     * 回复意见
     * @param opinionReplyDto
     * @return
     */
    RespBody replyOpinion(OpinionReplyDto opinionReplyDto);


    /**
     * 代表收到的意见
     * @param uidDto
     * @return
     */
    RespBody memberRecOpins(UidDto uidDto);


}
