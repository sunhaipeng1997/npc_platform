package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.vo.CountVo;
import org.springframework.data.jpa.repository.Query;

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

    @Query("select new com.cdkhd.npc.vo.CountVo(npc.education, count(npc.uid)) from NpcMember npc " +
            "where npc.area=?1 and npc.isDel=false and npc.status=1 " +
            "group by npc.education")
    List<CountVo> countEducation(Long areaId);
}
