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

    @Query(value = "select count(per.uid) from Performance as per where per.createTime >= ?1 and per.level = ?2 and per.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, Byte level, String uid);

    List<Performance> findByPerformanceTypeUid(String uid);

    @Query("select new com.cdkhd.npc.vo.CountVo(town.name, count(pfm.uid)) from Performance pfm, Town town " +
            "where pfm.town=town.id and pfm.area.id=?1 and pfm.isDel=false and pfm.level=1 and pfm.status=1 " +
            "group by pfm.town")
    List<CountVo> count4Town(Long areaId);

    @Query("select count(pfm.uid) from Performance pfm " +
            "where pfm.area.id=?1 and pfm.isDel=false and pfm.status=1")
    Integer countAll(Long areaId);
}
