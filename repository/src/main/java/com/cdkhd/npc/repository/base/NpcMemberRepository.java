package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.NpcMember;

import java.util.List;
import java.util.Set;

public interface NpcMemberRepository extends BaseRepository<NpcMember> {

    List<NpcMember> findByLevel(Byte level);

    List<NpcMember> findByTownUidAndLevelAndIsDelFalse(String uid, Byte level);

    List<NpcMember> findByAreaUidAndLevelAndIsDelFalse(String uid, Byte level);

    List<NpcMember> findByNpcMemberGroupUidAndIsDelFalse(String uid);

    NpcMember findByAccount(Account account);

    NpcMember findByLevelAndMobileAndUidIsNotAndIsDelFalse(Byte level, String mobile,String uid);

    NpcMember findByLevelAndMobileAndIsDelFalse(Byte level, String mobile);

    Set<NpcMember> findByUidIn(List<String> uids);

    List<NpcMember> findByMobile(String mobile);
}
