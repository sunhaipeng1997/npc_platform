package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Session;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.SessionRepository;
import com.cdkhd.npc.service.SessionService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionServiceImpl.class);

    private SessionRepository sessionRepository;

    @Autowired
    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * 获取届期列表
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @Override
    public RespBody getSessions(UserDetailsImpl userDetails) {
        RespBody<List<CommonVo>> body = new RespBody<>();
        List<Session> sessions;
        //如果当前后台管理员是镇后台管理员，则查询该镇的所有小组
        //如果当前后台管理员是区后台管理员，则查询该区的所有镇
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sessions = sessionRepository.findByTownUid(userDetails.getTown().getUid());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            sessions = sessionRepository.findByAreaUidAndLevel(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
        } else {
            throw new RuntimeException("当前后台管理员level不合法");
        }

        List<CommonVo> vos = sessions.stream().map(session ->
                CommonVo.convert(session.getUid(), session.getName())).collect(Collectors.toList());

        body.setData(vos);
        return body;
    }


}
