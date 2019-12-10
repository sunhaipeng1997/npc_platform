package com.cdkhd.npc.repository;

import com.cdkhd.npc.entity.AccountUP;

public interface AccountRepository extends BaseRepository<AccountUP> {
    AccountUP findByUsername(String username);

    AccountUP findByOpenId(String openid);
}
