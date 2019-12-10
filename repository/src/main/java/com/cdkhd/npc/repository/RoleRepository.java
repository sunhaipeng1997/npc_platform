package com.cdkhd.npc.repository;

import com.cdkhd.npc.entity.Role;

public interface RoleRepository extends BaseRepository<com.cdkhd.npc.entity.Role> {
    Role findByKeywordAndEnabledIsTrue(String keyword);
}
