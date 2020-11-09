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

    List<Suggestion> findBySuggestionBusinessUid(String businessUid);

    List<Suggestion> findBySuggestionBusinessUidAndStatusGreaterThanAndLevel(String businessUid, Byte status, Byte level);

    //统计该镇办理中的所有建议数量（包括状态为“办完”但还未“办结”的）
    @Query(value = "select  count (sug.uid) from Suggestion as sug where sug.town.uid = ?1 and sug.level = ?2 and sug.status in (3, 4, 5, 6)")
    Integer countTownDoingSugNum(String uid, Byte value);

    //统计该镇办结的所有建议数量
    @Query(value = "select  count (sug.uid) from Suggestion as sug where sug.town.uid = ?1 and sug.level = ?2 and sug.status in (7, 8)")
    Integer countTownFinishSugNum(String uid, Byte value);

    //统计该小组办理中的所有建议数量
    @Query(value = "select  count (sug.uid) from Suggestion as sug where sug.raiser.npcMemberGroup.uid = ?1 and sug.level = ?2 and sug.status in (3, 4, 5, 6)")
    Integer countGroupDoingSugNum(String uid, Byte value);

    //统计该小组办结的所有建议数量
    @Query(value = "select  count (sug.uid) from Suggestion as sug where sug.raiser.npcMemberGroup.uid = ?1 and sug.level = ?2 and sug.status in (7, 8)")
    Integer countGroupFinishSugNum(String uid, Byte value);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.createTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3")
    Integer countTownTodayNumber(Date today, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.createTime >= ?1 and sug.raiser.mobile in ?2 and sug.area.uid = ?3")
    Integer countAreaTodayNumber(Date today, List<String> mobile, String uid);

    //由于JPA查询结果只能映射实体类，故需要在sql中使用 new 语法
    @Query("select new com.cdkhd.npc.vo.CountVo(sug.town.name, count(sug.uid)) from Suggestion sug " +
            "where sug.area.id=?1 and sug.isDel=false and sug.level=2 and sug.status>=3 " +
            "group by sug.town.id")
    List<CountVo> countByArea(Long areaId);

    @Query("select new com.cdkhd.npc.vo.CountVo(sug.raiser.npcMemberGroup.name, count(sug.uid)) from Suggestion sug " +
            "where sug.isDel=false and sug.area.id=?1 and sug.town.uid=?2 and sug.status>=3 " +
            "group by sug.raiser.npcMemberGroup.id")
    List<CountVo> countByTown(Long areaId, String townUid);

    @Query("select new com.cdkhd.npc.vo.CountVo(sug.suggestionBusiness.name, count(sug.uid)) from Suggestion sug " +
            "where sug.area.id=?1 and sug.isDel=false and sug.level=2 and sug.status>=3 " +
            "group by sug.suggestionBusiness.id")
    List<CountVo> countByAreaType(Long areaId);

    @Query("select new com.cdkhd.npc.vo.CountVo(sug.suggestionBusiness.name, count(sug.uid)) from Suggestion sug " +
            "where sug.isDel=false and sug.area.id=?1 and sug.town.uid=?2 and sug.status>=3 " +
            "group by sug.suggestionBusiness.id")
    List<CountVo> countByTownType(Long areaId, String townUid);

    @Query("select count(sug.uid) from Suggestion sug " +
            "where sug.isDel=false and sug.level=2 and sug.area.id=?1 and sug.status>=3 ")
    Integer countAll4Area(Long areaId);

    @Query("select count(sug.uid) from Suggestion sug " +
            "where sug.isDel=false and sug.area.id=?1 and sug.town.uid=?2 and sug.status>=3 ")
    Integer countAll4Town(Long areaId, String townUid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.auditTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3")
    Integer countTownMonthNewNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.auditTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3")
    Integer countAreaMonthNewNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.accomplishTime >= ?1 and sug.level = ?2 and sug.town.uid = ?3")
    Integer countTownMonthCompletedNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.accomplishTime >= ?1 and sug.level = ?2 and sug.area.uid = ?3")
    Integer countAreaMonthCompletedNumber(Date date, Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.level = ?1 and sug.town.uid = ?2 and (sug.status = 4 or sug.status = 5 or sug.status = 6)")
    Integer countTownDealingNumber(Byte level, String uid);

    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.level = ?1 and sug.area.uid = ?2 and (sug.status = 4 or sug.status = 5 or sug.status = 6)")
    Integer countAreaDealingNumber(Byte level, String uid);

    //镇人大后台管理员查看本月新增的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.raiseTime < ?2 and sug.level = ?3 and sug.town.uid = ?4 and sug.status not in (-1, 0, 1, 2)")
    Integer adminCountTownMonthNewNumber(Date start, Date end, Byte level, String uid);

    //区人大后台管理员查看本月新增的建议
    @Query(value = "select count(sug.uid) from Suggestion as sug where sug.raiseTime >= ?1 and sug.raiseTime < ?2 and sug.level = ?3 and sug.area.uid = ?4 and sug.status not in (-1, 0, 1, 2)")
    Integer adminCountAreaMonthNewNumber(Date start, Date end, Byte level, String uid);

    List<Suggestion> findByExpectDateIsNotNullAndFinishTimeIsNullAndIsDelFalse();

}
