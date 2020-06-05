package com.cdkhd.npc.repository.suggestion_deal;
/*
 * @description:
 * @author:liyang
 * @create:2020-05-26
 */

import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface UnitSuggestionRepository extends BaseRepository<UnitSuggestion> {
    UnitSuggestion findByUid(String uid);

    List<UnitSuggestion> findBySuggestionUidAndType(String uid, Byte type);
}
