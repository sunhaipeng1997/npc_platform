package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Permission;

public interface PermissionRepository extends BaseRepository<Permission> {
    Permission findByKeyword(String keyword);
}
