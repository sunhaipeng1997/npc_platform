package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.PerformanceType;
import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.repository.base.BaseRepository;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerformanceTypeRepository extends BaseRepository<PerformanceType> {

    List<PerformanceType> findByEnabledTrue();

    @Query(value = "select type from PerformanceType as type where type.sequence < ?1 order by type.sequence desc ")
    PerformanceType findBySequenceDesc(Integer sequence, Pageable page);

    @Query(value = "select type from PerformanceType as type where type.sequence > ?1 order by type.sequence asc ")
    PerformanceType findBySequenceAsc(Integer sequence, Pageable page);
}
