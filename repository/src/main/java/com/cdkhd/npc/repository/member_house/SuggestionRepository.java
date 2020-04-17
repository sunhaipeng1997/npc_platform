package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.repository.base.BaseRepository;
import com.cdkhd.npc.vo.CountVo;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface SuggestionRepository extends BaseRepository<Suggestion> {

    Suggestion findByTransUid(String uid);

    Suggestion findByUidAndTransUid(String uid, String transUid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.createTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3")
    Integer countTownTodayNumber(Date today, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.createTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, Byte level, String uid);

    //由于JPA查询结果只能映射实体类，故需要在sql中使用 new 语法
    @Query("select new com.cdkhd.npc.vo.CountVo(town.name, count(sug.uid)) from Suggestion sug, Town town " +
            "where sug.town=town.id and sug.area.id=?1 and sug.isDel=false and sug.level=1 and sug.status>=3 " +
            "group by sug.town")
    List<CountVo> count4Town(Long areaId);

    @Query("select new com.cdkhd.npc.vo.CountVo(stype.name, count(sug.uid)) from Suggestion sug, SuggestionBusiness stype " +
            "where sug.suggestionBusiness=stype.id and sug.area.id=?1 and sug.isDel=false and sug.level=1 and sug.status>=3 " +
            "group by sug.suggestionBusiness")
    List<CountVo> count4Type(Long areaId);

    @Query("select count(sug.uid) from Suggestion sug " +
            "where sug.area.id=?1 and sug.isDel=false and sug.status>=3 ")
    Integer countAll(Long areaId);
}
