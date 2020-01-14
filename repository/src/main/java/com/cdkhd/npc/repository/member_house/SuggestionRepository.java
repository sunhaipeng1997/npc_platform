package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface SuggestionRepository extends BaseRepository<Suggestion> {

    Suggestion findByTransUid(String uid);

    Suggestion findByUidAndTransUid(String uid, String transUid);
}
