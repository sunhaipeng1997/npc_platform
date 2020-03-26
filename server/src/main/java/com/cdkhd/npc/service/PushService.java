package com.cdkhd.npc.service;

import com.cdkhd.npc.entity.Account;

public interface PushService {

    void pushMsg(Account account, String msg, Integer type, String keyWord);

}
