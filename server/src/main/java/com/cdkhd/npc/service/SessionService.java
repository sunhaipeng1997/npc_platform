package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Session;
import com.cdkhd.npc.entity.dto.SessionAddDto;
import com.cdkhd.npc.entity.dto.SessionPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface SessionService {

    /**
     * 获取届期下拉
     * @param userDetails
     * @return
     */
    RespBody getSessions(UserDetailsImpl userDetails);

    /**
     * 条件查询届期分页
     * @param userDetails
     * @return
     */
    RespBody sessionPage(UserDetailsImpl userDetails, SessionPageDto sessionPageDto);

    /**
     * 添加、修改届期
     * @param userDetails
     * @return
     */
    RespBody addOrUpdateSession(UserDetailsImpl userDetails, SessionAddDto sessionAddDto);

    /**
     * 删除届期
     * @param userDetails
     * @return
     */
    RespBody deleteSessions(UserDetailsImpl userDetails,String uid);

    /**
     * 换届
     * @param userDetails
     * @return
     */
    RespBody clearSessions(UserDetailsImpl userDetails);

    /**
     * 当前届期uid
     * @param userDetails
     * @return
     */
    RespBody getCurrentSession(UserDetailsImpl userDetails);


    /**
     * 当前届期
     * @param userDetails
     * @return
     */
    Session currentSession(UserDetailsImpl userDetails);

    /**
     * 默认届期
     * @param userDetails
     * @return
     */
    Session defaultSession(UserDetailsImpl userDetails);
}
