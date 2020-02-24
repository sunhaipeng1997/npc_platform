package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NpcMemberRole;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface NpcMemberRoleRepository extends BaseRepository<NpcMemberRole> {
    NpcMemberRole findByKeyword(String keyword);

    Set<NpcMemberRole> findByKeywordIn(String keyword);

    @Query(value = "select role from NpcMemberRole role where role.permissions in (select permission from Permission permission where permission.keyword = ?1)")
    List<NpcMemberRole> findByPermissionsKeyword(String keyword);

    List<NpcMemberRole> findByIsMustTrue();

}
