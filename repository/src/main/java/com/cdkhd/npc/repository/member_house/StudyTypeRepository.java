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

    @Query(value = "select max(type.sequence) from StudyType type where type.level =?1 and type.area.uid = ?2")
    Integer findMaxSequenceByLevelAndAreaUid(Byte level,String areaUid);

    @Query(value = "select max(type.sequence) from StudyType type where type.level =?1 and type.town.uid = ?2")
    Integer findMaxSequenceByLevelAndTownUid(Byte level,String townUid);

    @Query(value = "select type from StudyType as type where type.sequence < ?1 and type.level = ?2 and type.town.uid = ?3 order by type.sequence desc ")
    Page<StudyType> findByTownSequenceDesc(Integer sequence, Byte level, String townUid, Pageable page);

    @Query(value = "select type from StudyType as type where type.sequence < ?1 and type.level = ?2 and type.area.uid = ?3 order by type.sequence desc ")
    Page<StudyType> findByAreaSequenceDesc(Integer sequence, Byte level, String areaUid, Pageable page);

    @Query(value = "select type from StudyType as type where type.sequence > ?1 and type.level = ?2 and type.town.uid = ?3 order by type.sequence asc ")
    Page<StudyType> findByTownSequenceAsc(Integer sequence, Byte level, String townUid, Pageable page);

    @Query(value = "select type from StudyType as type where type.sequence > ?1 and type.level = ?2 and type.area.uid = ?3 order by type.sequence asc ")
    Page<StudyType> findByAreaSequenceAsc(Integer sequence, Byte level, String areaUid, Pageable page);

    List<StudyType> findByLevelAndTownUidAndStatusAndIsDelFalse(Byte level, String uid, byte value);

    List<StudyType> findByLevelAndAreaUidAndStatusAndIsDelFalse(Byte level, String uid, byte value);

}
