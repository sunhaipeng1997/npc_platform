package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface AccountRepository extends BaseRepository<Account> {

    Account findByLoginUPUsername(String username);

    Account findByLoginWeChatOpenId(String openid);
}
