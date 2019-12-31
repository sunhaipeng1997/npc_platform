package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NpcMemberRole;

public interface NpcMemberRoleRepository extends BaseRepository<NpcMemberRole> {
    NpcMemberRole findByKeyword(String keyword);
}
