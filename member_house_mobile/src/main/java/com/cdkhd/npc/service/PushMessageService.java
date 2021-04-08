package com.cdkhd.npc.service;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.Account;

public interface PushMessageService {

     void pushMsg(Account receiverAccount, int msgType, JSONObject content) ;

}
