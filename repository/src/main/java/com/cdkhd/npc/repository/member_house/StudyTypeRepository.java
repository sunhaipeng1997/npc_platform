package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.StudyType;
import com.cdkhd.npc.repository.base.BaseRepository;
import com.cdkhd.npc.vo.PageVo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudyTypeRepository extends BaseRepository<StudyType> {

    List<StudyType> findByStatusAndLevelAndTownUidOrderBySequenceAsc(Byte status, Byte level, String town);

    List<StudyType> findByStatusAndLevelAndAreaUidOrderBySequenceAsc(Byte status, Byte level, String area);

    StudyType findByNameAndLevelAndTownUidAndIsDelFalse(String name, Byte level, String uid);

    StudyType findByNameAndLevelAndAreaUidAndIsDelFalse(String name, Byte level, String uid);

    @Query(value = "select max(type.sequence) from PerformanceType type")
    Integer findMaxSequence();

    @Query(value = "select type from PerformanceType as type where type.sequence < ?1 order by type.sequence desc ")
    PageVo<StudyType> findBySequenceDesc(Integer sequence, Pageable page);

    @Query(value = "select type from PerformanceType as type where type.sequence > ?1 order by type.sequence asc ")
    PageVo<StudyType> findBySequenceAsc(Integer sequence, Pageable page);

    List<StudyType> findByLevelAndTownUidAndIsDelFalse(Byte level, String uid);

    List<StudyType> findByLevelAndAreaUidAndIsDelFalse(Byte level, String uid);
}
