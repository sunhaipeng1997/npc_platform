package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NpcMember;

import java.util.List;

public interface NpcMemberRepository extends BaseRepository<NpcMember> {

    List<NpcMember> findByLevel(Byte level);

}
