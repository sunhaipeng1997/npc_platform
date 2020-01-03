package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NpcMemberRole;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NpcMemberRoleRepository extends BaseRepository<NpcMemberRole> {
    NpcMemberRole findByKeyword(String keyword);

    @Query(value = "select role from NpcMemberRole role where role.permissions in (select permission from Permission permission where permission.keyword = ?1)")
    List<NpcMemberRole> findByPermissionsKeyword(String keyword);
}
