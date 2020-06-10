package com.cdkhd.npc.repository.suggestion_deal;
/*
 * @description:
 * @author:liyang
 * @create:2020-05-26
 */

import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.util.List;

public interface UnitSuggestionRepository extends BaseRepository<UnitSuggestion> {

    UnitSuggestion findByUid(String uid);


    @Query(value = "select distinct unitSug.suggestion from UnitSuggestion as unitSug where unitSug.unit.uid = ?1 and (unitSug.suggestion.status = 4 or unitSug.suggestion.status = 5)")
    List<Suggestion> findDealingSuggestionByUnitUid(String unitUid);

    @Query(value = "select distinct unitSug.suggestion from UnitSuggestion as unitSug where unitSug.unit.uid = ?1 and unitSug.suggestion.status = 7")
    List<Suggestion> findCompletedSuggestionByUnitUid(String unitUid);

    List<UnitSuggestion> findBySuggestionUidAndType(String uid, Byte type);
}
