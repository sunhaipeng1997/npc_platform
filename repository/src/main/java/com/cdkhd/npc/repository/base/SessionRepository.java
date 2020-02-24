package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Session;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface SessionRepository extends BaseRepository<Session> {
    List<Session> findByTownUidAndLevel(String townUid, Byte level);

    List<Session> findByAreaUidAndLevel(String townUid, Byte level);

    List<Session> findByTownUidAndLevelAndUidNotAndStartDateIsNotNull(String townUid, Byte level,String uid);

    List<Session> findByAreaUidAndLevelAndUidNotAndStartDateIsNotNull(String townUid, Byte level,String uid);

    List<Session> findByTownUidAndLevelAndStartDateIsNotNull(String townUid, Byte level);

    List<Session> findByAreaUidAndLevelAndStartDateIsNotNull(String townUid, Byte level);

    Session findByTownUidAndLevelAndStartDateIsNullAndEndDateIsNull(String townUid, Byte level);

    Session findByAreaUidAndLevelAndStartDateIsNullAndEndDateIsNull(String townUid, Byte level);

    @Query(value = "select s from Session s where s.town.uid = ?1 and s.level = ?2 and ?3 between s.startDate and s.endDate")
    Session findTownCurrentSession(String townUid, Byte level, Date date);

    @Query(value = "select s from Session s where s.area.uid = ?1 and s.level = ?2 and ?3 between s.startDate and s.endDate")
    Session findAreaCurrentSession(String areaUid, Byte level, Date date);

    Set<Session> findByUidIn(List<String> sessionUids);
}
