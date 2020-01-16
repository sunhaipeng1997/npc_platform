package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.OpinionPageDto;
import com.cdkhd.npc.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface OpinionService {

    /**
     * 条件查询所有意见
     * @param userDetails
     * @param opinionPageDto
     * @return
     */
    RespBody opinionPage(UserDetailsImpl userDetails, OpinionPageDto opinionPageDto);

    /**
     * 回复意见
     * @param opinionPageDto
     * @return
     */
    void exportOpinion(UserDetailsImpl userDetails, OpinionPageDto opinionPageDto, HttpServletRequest req, HttpServletResponse res);
}
