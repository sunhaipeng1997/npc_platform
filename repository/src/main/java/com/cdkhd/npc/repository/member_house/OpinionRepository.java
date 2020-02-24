package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface OpinionRepository extends BaseRepository<Opinion> {


    List<Opinion> findBySenderUid(String uid);

    Opinion findByTransUid(String uid);

    @Query(value = "select count(opinion.uid) from Opinion as opinion where opinion.createTime = ?1 and opinion.level = ?2 and opinion.town.uid = ?3")
    Integer countTownTodayNumber(Date today, Byte level, String uid);

    @Query(value = "select count(opinion.uid) from Opinion as opinion where opinion.createTime = ?1 and opinion.level = ?2 and opinion.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, Byte level, String uid);
}
