package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.StudyType;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudyTypeRepository extends BaseRepository<StudyType> {

    List<StudyType> findByStatusAndLevelAndTownUidOrderBySequenceAsc(Byte status, Byte level, String town);

    List<StudyType> findByStatusAndLevelAndAreaUidOrderBySequenceAsc(Byte status, Byte level, String area);

    StudyType findByNameAndLevelAndTownUidAndIsDelFalse(String name, Byte level, String uid);

    StudyType findByNameAndLevelAndAreaUidAndIsDelFalse(String name, Byte level, String uid);

    StudyType findByNameAndLevelAndTownUidAndIsDelFalseAndUidNot(String name, Byte level, String uid,String typeUid);

    StudyType findByNameAndLevelAndAreaUidAndIsDelFalseAndUidNot(String name, Byte level, String uid,String typeUid);

    @Query(value = "select max(type.sequence) from StudyType type")
    Integer findMaxSequence();

    @Query(value = "select type from StudyType as type where type.sequence < ?1 order by type.sequence desc ")
    Page<StudyType> findBySequenceDesc(Integer sequence, Pageable page);

    @Query(value = "select type from StudyType as type where type.sequence > ?1 order by type.sequence asc ")
    Page<StudyType> findBySequenceAsc(Integer sequence, Pageable page);

    List<StudyType> findByLevelAndTownUidAndIsDelFalse(Byte level, String uid);

    List<StudyType> findByLevelAndAreaUidAndIsDelFalse(Byte level, String uid);
}
