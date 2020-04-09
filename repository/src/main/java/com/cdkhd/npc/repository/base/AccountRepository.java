package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Account;

import java.util.List;

public interface AccountRepository extends BaseRepository<Account> {
    List<Account> findByMobile(String mobile);

    Account findByUsernameAndMobile(String username, String mobile);

    Account findByUsername(String username);
}
