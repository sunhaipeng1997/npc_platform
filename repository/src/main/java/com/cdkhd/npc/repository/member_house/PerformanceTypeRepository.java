package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.PerformanceType;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerformanceTypeRepository extends BaseRepository<PerformanceType> {

    @Query(value = "select type from PerformanceType as type where type.sequence < ?1 and type.level = ?2 and type.area.uid = ?3 order by type.sequence desc ")
    Page<PerformanceType> findBySequenceAndLevelAreaUidDesc(Integer sequence,Byte level, String uid, Pageable page);

    @Query(value = "select type from PerformanceType as type where type.sequence < ?1 and type.level = ?2 and type.town.uid = ?3 order by type.sequence desc ")
    Page<PerformanceType> findBySequenceAndLevelTownUidDesc(Integer sequence,Byte level, String uid, Pageable page);

    @Query(value = "select type from PerformanceType as type where type.sequence > ?1 and type.level = ?2 and type.area.uid = ?3 order by type.sequence asc ")
    Page<PerformanceType> findBySequenceAndLevelAreaUidAsc(Integer sequence,Byte level, String uid, Pageable page);

    @Query(value = "select type from PerformanceType as type where type.sequence > ?1 and type.level = ?2 and type.town.uid = ?3 order by type.sequence asc ")
    Page<PerformanceType> findBySequenceAndLevelTownUidAsc(Integer sequence,Byte level, String uid, Pageable page);

    PerformanceType findByNameAndLevelAndTownUidAndIsDelFalse(String name, Byte level, String townUid);

    PerformanceType findByNameAndLevelAndAreaUidAndIsDelFalse(String name, Byte level, String areaUid);

    List<PerformanceType> findByLevelAndTownUidAndIsDelFalse(Byte level, String townUid);

    List<PerformanceType> findByLevelAndAreaUidAndIsDelFalse(Byte level, String areaUid);

    List<PerformanceType> findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(Byte level, String townUid,Byte status);

    List<PerformanceType> findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(Byte level, String areaUid,Byte status);

    //查询是否初始化区上的履职类型
    PerformanceType findByNameAndLevelAndAreaUidAndStatusAndIsDelFalse(String name, Byte level, String uid,Byte status);

    //查询是否初始化某个镇上的履职类型
    PerformanceType findByNameAndLevelAndTownUidAndStatusAndIsDelFalse(String name, Byte level,String town, Byte status);

    @Query(value = "select max(type.sequence) from PerformanceType type where type.level = ?1 and type.town.uid = ?2")
    Integer findMaxSequenceByLevelAndTownUid(Byte level, String uid);

    @Query(value = "select max(type.sequence) from PerformanceType type where type.level = ?1 and type.area.uid = ?2")
    Integer findMaxSequenceByLevelAndAreaUid(Byte value, String uid);

    @Query(value = "select max(type.sequence) from PerformanceType type where type.level = ?1 and type.area.name = ?2")
    Integer findMaxSequenceByLevelAndAreaName(Byte value, String name);

    PerformanceType findByNameAndTownUid(String typeName, String townUid);

    PerformanceType findByNameAndAreaUid(String typeName, String areaUid);

    PerformanceType findByNameAndLevelAndTownUidAndIsDelFalseAndUidIsNot(String name, Byte level, String townUid, String uid);

    PerformanceType findByNameAndLevelAndAreaUidAndIsDelFalseAndUidIsNot(String name, Byte level, String areaUid, String uid);

    PerformanceType findByNameAndLevelAndAreaUidAndStatusAndIsDelFalseAndIsDefaultIsTrue(String value, Byte value1, String uid, byte value2);

    PerformanceType findByNameAndTownName(String typeName, String townName);

}
