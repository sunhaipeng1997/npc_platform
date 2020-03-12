package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

import java.util.List;

public interface SpecialFunctionService {

    RespBody getSettings(UserDetailsImpl userDetails);

    RespBody newsAuditor(UserDetailsImpl userDetails, List<String> uids);

    RespBody notificationAuditor(UserDetailsImpl userDetails, List<String> uids);

    RespBody adviceReceiver(UserDetailsImpl userDetails, List<String> uids);

    RespBody performanceAuditorManager(UserDetailsImpl userDetails, List<String> uids);

    RespBody performanceGroupAuditor(String group, String uid);

    RespBody performanceTownAuditor(String town, String uid);

    RespBody auditorSwitch(UserDetailsImpl userDetails, Boolean switches);

}
