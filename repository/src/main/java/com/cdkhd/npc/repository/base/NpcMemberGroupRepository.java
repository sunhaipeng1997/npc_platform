package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NpcMemberGroup;

public interface NpcMemberGroupRepository extends BaseRepository<NpcMemberGroup> {

    NpcMemberGroup findByTownUidAndName(String townUid, String name);
}
