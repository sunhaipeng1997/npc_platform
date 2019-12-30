package com.cdkhd.npc.service;


import com.cdkhd.npc.entity.Account;

/**
 * @创建人
 * @创建时间 2018/10/26
 * @描述
 */
public interface PushService {

    void pushMsg(Account account, String msg, Integer type, String keyWord);

}
