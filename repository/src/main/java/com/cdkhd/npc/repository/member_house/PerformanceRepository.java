package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Performance;
import com.cdkhd.npc.repository.base.BaseRepository;
import com.cdkhd.npc.vo.CountVo;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface PerformanceRepository extends BaseRepository<Performance> {

    Performance findByTransUid(String transUid);

    Performance findByUidAndTransUid(String uid, String transUid);

    @Query(value = "select count(per.uid) from Performance as per where per.createTime >= ?1 and per.level = ?2 and per.town.uid = ?3")
    Integer countTownTodayNumber(Date today, Byte level, String uid);

    @Query(value = "select count(per.uid) from Performance as per where per.createTime >= ?1 and per.npcMember.mobile in(?2) and per.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, List<String> mobile, String uid);

    List<Performance> findByPerformanceTypeUidAndLevelAndAreaUidAndIsDelFalse(String uid,Byte level,String areaUid);

    List<Performance> findByPerformanceTypeUidAndLevelAndTownUidAndIsDelFalse(String uid,Byte level,String townUid);

    @Query("select new com.cdkhd.npc.vo.CountVo(pfm.town.name, count(pfm.uid)) from Performance pfm " +
            "where pfm.area.id=?1 and pfm.isDel=false and pfm.level=2 and pfm.status>=3 " +
            "group by pfm.town.id")
    List<CountVo> countByArea(Long areaId);

    @Query("select new com.cdkhd.npc.vo.CountVo(pfm.npcMember.npcMemberGroup.name, count(pfm.uid)) from Performance pfm " +
            "where pfm.isDel=false and pfm.level=1 and pfm.area.id=?1 and pfm.town.uid=?2 and pfm.status>=3 " +
            "group by pfm.npcMember.npcMemberGroup.id")
    List<CountVo> countByTown(Long areaId, String townUid);

    @Query("select count(pfm.uid) from Performance pfm " +
            "where pfm.level=2 and pfm.area.id=?1 and pfm.isDel=false and pfm.status>=3")
    Integer countAll4Area(Long areaId);

    @Query("select count(pfm.uid) from Performance pfm " +
            "where pfm.level=1 and pfm.area.id=?1 and pfm.town.uid=?2 and pfm.isDel=false and pfm.status>=3")
    Integer countAll4Town(Long areaId, String townUid);

    List<Performance> findByNpcMemberUidAndIsDelFalse(String uid);
}
