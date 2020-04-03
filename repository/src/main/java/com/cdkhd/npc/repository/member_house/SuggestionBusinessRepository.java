package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface SuggestionBusinessRepository extends BaseRepository<SuggestionBusiness> {

    @Query(value = "select type from SuggestionBusiness as type where type.sequence < ?1 and type.level = ?2 and type.area.uid = ?3 order by type.sequence desc ")
    Page<SuggestionBusiness> findBySequenceAndLevelAreaUidDesc(Integer sequence, Byte level, String uid, Pageable page);

    @Query(value = "select type from SuggestionBusiness as type where type.sequence < ?1 and type.level = ?2 and type.town.uid = ?3 order by type.sequence desc ")
    Page<SuggestionBusiness> findBySequenceAndLevelTownUidDesc(Integer sequence, Byte level, String uid, Pageable page);

    @Query(value = "select type from SuggestionBusiness as type where type.sequence > ?1 and type.level = ?2 and type.area.uid = ?3 order by type.sequence asc ")
    Page<SuggestionBusiness> findBySequenceAndLevelAreaUidAsc(Integer sequence, Byte level, String uid, Pageable page);

    @Query(value = "select type from SuggestionBusiness as type where type.sequence > ?1 and type.level = ?2 and type.town.uid = ?3 order by type.sequence asc ")
    Page<SuggestionBusiness> findBySequenceAndLevelTownUidAsc(Integer sequence, Byte level, String uid, Pageable page);

    @Query(value = "select max(sb.sequence) from SuggestionBusiness sb")
    Integer findMaxSequence();

    List<SuggestionBusiness> findByLevelAndTownUidAndIsDelFalse(Byte level, String townUid);

    List<SuggestionBusiness> findByLevelAndAreaUidAndIsDelFalse(Byte level, String areaUid);

    SuggestionBusiness findByNameAndLevelAndTownUidAndIsDelFalse(String name, Byte level, String townUid);

    SuggestionBusiness findByNameAndLevelAndAreaUidAndIsDelFalse(String name, Byte level, String areaUid);

    Set<SuggestionBusiness> findByTownUid(String townUid);

    Set<SuggestionBusiness> findByAreaUid(String areaUid);
}
