package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.repository.base.BaseRepository;
import com.cdkhd.npc.vo.CountVo;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface OpinionRepository extends BaseRepository<Opinion> {


    List<Opinion> findBySenderUid(String uid);

    Opinion findByTransUid(String uid);

    @Query(value = "select count(opinion.uid) from Opinion as opinion where opinion.createTime >= ?1 and opinion.level = ?2 and opinion.town.uid = ?3")
    Integer countTownTodayNumber(Date today, Byte level, String uid);

    @Query(value = "select count(opinion.uid) from Opinion as opinion where opinion.createTime >= ?1 and opinion.receiver.mobile in(?2) and opinion.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, List<String> mobile, String uid);

    @Query("select new com.cdkhd.npc.vo.CountVo(op.town.name, count(op.uid)) from Opinion op " +
            "where op.area.id=?1 and op.isDel=false and op.level=2 " +
            "group by op.town.id")
    List<CountVo> countByArea(Long areaId);

    @Query("select new com.cdkhd.npc.vo.CountVo(op.receiver.npcMemberGroup.name, count(op.uid)) from Opinion op " +
            "where op.isDel=false and op.level=1 and op.area.id=?1 and op.town.uid=?2 " +
            "group by op.receiver.npcMemberGroup.id")
    List<CountVo> countByTown(Long areaId, String townUid);

    @Query("select count(op.uid) from Opinion op " +
            "where op.level=2 and op.area.id=?1 and op.isDel=false")
    Integer countAll4Area(Long areaId);

    @Query("select count(op.uid) from Opinion op " +
            "where op.level=1 and op.area.id=?1 and op.town.uid=?2 and op.isDel=false")
    Integer countAll4Town(Long areaId, String townUid);
}
