package com.cdkhd.npc.repository.suggestion_deal;

import com.cdkhd.npc.entity.Urge;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface UrgeRepository extends BaseRepository<Urge> {

    List<Urge> findBySuggestionUidAndType(String uid, Byte type);

}
