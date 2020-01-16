package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.PerformanceType;
import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.repository.base.BaseRepository;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerformanceTypeRepository extends BaseRepository<PerformanceType> {

    List<PerformanceType> findByStatus(Byte status);

    @Query(value = "select type from PerformanceType as type where type.sequence < ?1 order by type.sequence desc ")
    Page<PerformanceType> findBySequenceDesc(Integer sequence, Pageable page);

    @Query(value = "select type from PerformanceType as type where type.sequence > ?1 order by type.sequence asc ")
    Page<PerformanceType> findBySequenceAsc(Integer sequence, Pageable page);

    PerformanceType findByNameAndLevelAndTownUidAndIsDelFalse(String name, Byte level, String townUid);

    PerformanceType findByNameAndLevelAndAreaUidAndIsDelFalse(String name, Byte level, String areaUid);

    @Query(value = "select max(type.sequence) from PerformanceType type")
    Integer findMaxSequence();

    List<PerformanceType> findByLevelAndTownUidAndIsDelFalse(Byte level, String townUid);

    List<PerformanceType> findByLevelAndAreaUidAndIsDelFalse(Byte level, String areaUid);

    List<PerformanceType> findByLevelAndTownUidAndStatusAndIsDelFalse(Byte level, String townUid,Byte status);

    List<PerformanceType> findByLevelAndAreaUidAndStatusAndIsDelFalse(Byte level, String areaUid,Byte status);

}
