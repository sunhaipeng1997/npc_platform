package com.cdkhd.npc.repository.suggestion_deal;
/*
 * @description:
 * @author:liyang
 * @create:2020-05-26
 */

import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface UnitSuggestionRepository extends BaseRepository<UnitSuggestion> {
    UnitSuggestion findByUid(String uid);
}
