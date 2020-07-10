package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

import java.util.List;

public interface SpecialFunctionService {

    RespBody getSettings(UserDetailsImpl userDetails);

    RespBody newsAuditor(UserDetailsImpl userDetails, List<String> uids);


}
