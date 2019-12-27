package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NpcMemberRepository extends BaseRepository<NpcMember> {

    List<NpcMember> findByLevel(Byte level);

//    @Query(value = "select member.account.uid from NpcMember member where member.level = ?1")
//    List<String> findAccountUidByLevel(Byte level);
}
