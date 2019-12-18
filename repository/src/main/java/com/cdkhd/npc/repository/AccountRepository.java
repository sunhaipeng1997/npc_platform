package com.cdkhd.npc.repository;

import com.cdkhd.npc.entity.Account;

public interface AccountRepository extends BaseRepository<Account> {

    Account findByLoginUPUsername(String username);

    Account findByLoginWeChatOpenId(String openid);
}
