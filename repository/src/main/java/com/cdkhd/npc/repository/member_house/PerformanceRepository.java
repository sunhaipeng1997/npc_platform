package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Performance;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface PerformanceRepository extends BaseRepository<Performance> {

    Performance findByTransUid(String transUid);

    Performance findByUidAndTransUid(String uid, String transUid);

    @Query(value = "select count(per.uid) from Performance as per where per.createTime = ?1 and per.level = ?2 and per.town.uid = ?3")
    Integer countTownTodayNumber(Date today, Byte level, String uid);

    @Query(value = "select count(per.uid) from Performance as per where per.createTime = ?1 and per.level = ?2 and per.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, Byte level, String uid);

    List<Performance> findByPerformanceTypeUid(String uid);
}
