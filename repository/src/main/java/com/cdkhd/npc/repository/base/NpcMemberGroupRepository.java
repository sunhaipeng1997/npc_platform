package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NpcMemberGroup;

import java.util.List;

public interface NpcMemberGroupRepository extends BaseRepository<NpcMemberGroup> {

    NpcMemberGroup findByTownUidAndName(String townUid, String name);

    List<NpcMemberGroup> findByTownUid(String townUid);
}
