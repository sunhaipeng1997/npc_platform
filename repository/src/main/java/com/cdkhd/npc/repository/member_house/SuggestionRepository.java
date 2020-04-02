package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface SuggestionRepository extends BaseRepository<Suggestion> {

    Suggestion findByTransUid(String uid);

    Suggestion findByUidAndTransUid(String uid, String transUid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.createTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3")
    Integer countTownTodayNumber(Date today, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.createTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, Byte level, String uid);
}
