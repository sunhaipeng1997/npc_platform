package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.LoginWeChat;

public interface LoginWeChatRepository extends BaseRepository<LoginWeChat>{
    LoginWeChat findByUnionId(String unionId);

    LoginWeChat findByOpenId(String openId);
}
