package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SuggestionBusinessRepository extends BaseRepository<SuggestionBusiness> {

    @Query(value = "select sb from SuggestionBusiness as sb where sb.sequence < ?1 order by sb.sequence desc ")
    Page<SuggestionBusiness> findBySequenceDesc(Integer sequence, Pageable page);

    @Query(value = "select sb from SuggestionBusiness as sb where sb.sequence < ?1 order by sb.sequence asc ")
    Page<SuggestionBusiness> findBySequenceAsc(Integer sequence, Pageable page);

    @Query(value = "select max(sb.sequence) from SuggestionBusiness sb")
    Integer findMaxSequence();

    List<SuggestionBusiness> findByLevelAndTownUidAndIsDelFalse(Byte level, String townUid);

    List<SuggestionBusiness> findByLevelAndAreaUidAndIsDelFalse(Byte level, String areaUid);

    SuggestionBusiness findByNameAndLevelAndTownUidAndIsDelFalse(String name, Byte level, String townUid);

    SuggestionBusiness findByNameAndLevelAndAreaUidAndIsDelFalse(String name, Byte level, String areaUid);
}
