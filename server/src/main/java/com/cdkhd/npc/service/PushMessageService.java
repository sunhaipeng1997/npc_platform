package com.cdkhd.npc.service;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.enums.MsgTypeEnum;

public interface PushMessageService {

     void pushMsg(Account receiverAccount, int msgType,JSONObject content ) ;

}
