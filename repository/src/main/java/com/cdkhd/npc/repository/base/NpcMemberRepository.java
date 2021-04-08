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

    List<NpcMember> findByTownUidAndLevelAndStatusAndIsDelFalse(String uid, Byte level, Byte status);

    List<NpcMember> findByAreaUidAndLevelAndStatusAndIsDelFalse(String uid, Byte level, Byte status);

    List<NpcMember> findByAreaUidAndTownUidAndLevelAndStatusAndIsDelFalse(String uid, String townUid,Byte level, Byte status);

    List<NpcMember> findByNpcMemberGroupUidAndIsDelFalse(String uid);

    NpcMember findByLevelAndMobileAndUidIsNotAndIsDelFalse(Byte level, String mobile,String uid);

    NpcMember findByLevelAndMobileAndIsDelFalse(Byte level, String mobile);

    List<NpcMember> findByMobileAndIsDelFalse(String mobile);

    List<NpcMember> findByMobileAndAndUidIsNotAndIsDelFalse(String mobile,String uid);

    Set<NpcMember> findByUidIn(List<String> uids);

    List<NpcMember> findByTownType(Byte type);


    @Query("select new com.cdkhd.npc.vo.CountVo(npc.education, count(npc.uid)) from NpcMember npc " +
            "where npc.area.uid=?1 and npc.level = ?2 and npc.isDel=false and npc.status=1 " +
            "group by npc.education")
    List<CountVo> countEducation(String areaUid,Byte level);

    @Query("select new com.cdkhd.npc.vo.CountVo(npc.education, count(npc.uid)) from NpcMember npc " +
            "where npc.area.uid=?1 and npc.town.uid = ?2 and npc.level = ?3 and npc.isDel=false and npc.status=1 " +
            "group by npc.education")
    List<CountVo> countEducation(String areaUid,String townUid,Byte level);

    List<NpcMember> findByMobileAndUidIsNotAndIsDelFalse(String mobile, String uid);
}
