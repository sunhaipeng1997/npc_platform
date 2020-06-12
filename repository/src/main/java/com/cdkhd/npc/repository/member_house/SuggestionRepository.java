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

    List<Suggestion> findByRaiserUid(String npcUid);

    List<Suggestion> findBySuggestionBusinessUidAndStatusGreaterThanEqualAndStatusNot(String businessUid,Byte status,Byte not);


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


    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.auditTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3")
    Integer countTownMonthNewNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.auditTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3")
    Integer countAreaMonthNewNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.accomplishTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3")
    Integer countTownMonthCompletedNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.accomplishTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3")
    Integer countAreaMonthCompletedNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.level = ?1 and sug.town.uid = ?2 and (sug.status = 4 or sug.status = 5)")
    Integer countTownDealingNumber(Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.level = ?1 and sug.area.uid = ?2 and (sug.status = 4 or sug.status = 5)")
    Integer countAreaDealingNumber(Byte level, String uid);

    //镇人大后台管理员查看本月新增的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.raiseTime < ?2 and sug.level = ?3 and sug.town.uid = ?4 and sug.status not in (-1, 0, 1, 2)")
    Integer adminCountTownMonthNewNumber(Date start, Date end, Byte level, String uid);

    //镇人大后台管理员查看本月审核通过的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3 and sug.status in (3, 4, 5, 6, 7, 8)")
    Integer adminCountTownMonthAuditPassNumber(Date date, Byte level, String uid);

    //镇人大后台管理员查看本月审核不通过的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3 and sug.status = -1")
    Integer adminCountTownAuditRefuseNumber(Date date, Byte level, String uid);

    //区人大后台管理员查看本月新增的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.raiseTime < ?2 and sug.level = ?3 and sug.area.uid = ?4 and sug.status not in (-1, 0, 1, 2)")
    Integer adminCountAreaMonthNewNumber(Date start, Date end, Byte level, String uid);

    //区人大后台管理员查看本月审核通过的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3 and sug.status in (3, 4, 5, 6, 7, 8)")
    Integer adminCountAreaMonthAuditPassNumber(Date date, Byte level, String uid);

    //区人大后台管理员查看本月审核不通过的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3 and sug.status = -1")
    Integer adminCountAreaAuditRefuseNumber(Date date, Byte level, String uid);

}
