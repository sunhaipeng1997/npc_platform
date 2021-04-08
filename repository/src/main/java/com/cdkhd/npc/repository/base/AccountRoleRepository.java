package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.AccountRole;

public interface AccountRoleRepository extends BaseRepository<AccountRole> {

    AccountRole findByKeyword(String keyword);

}
