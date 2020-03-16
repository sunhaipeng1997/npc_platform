package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Account;

public interface AccountRepository extends BaseRepository<Account> {
    Account findByMobile(String mobile);
}
