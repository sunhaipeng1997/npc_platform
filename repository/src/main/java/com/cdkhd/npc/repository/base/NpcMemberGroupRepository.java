package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NpcMemberGroup;

import java.util.List;

public interface NpcMemberGroupRepository extends BaseRepository<NpcMemberGroup> {
    List<NpcMemberGroup> findByTownUid(String townUid);
}
