package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AccountPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface AccountService {

    RespBody findAccount(AccountPageDto accountPageDto);

    RespBody changeStatus(String uid, Byte status);

    RespBody getMyInfo(String uid);
}
