package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.AccountRole;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface AccountRoleRepository extends BaseRepository<AccountRole> {
    AccountRole findByKeyword(String keyword);
}
