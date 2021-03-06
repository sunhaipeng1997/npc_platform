package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AccountPageDto;
import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.vo.RespBody;

public interface AccountService {

    RespBody findAccount(UserDetailsImpl userDetails, AccountPageDto accountPageDto);

    RespBody changeStatus(String uid, Byte status);

    RespBody getMyInfo(String uid);

    RespBody updateInfo(UserDetailsImpl userDetails, UserInfoDto userInfoDto);

}
