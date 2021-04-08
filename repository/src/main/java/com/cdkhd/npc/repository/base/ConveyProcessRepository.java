package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.ConveyProcess;

import java.util.List;

public interface ConveyProcessRepository extends BaseRepository<ConveyProcess> {
    List<ConveyProcess> findBySuggestionId(Long id);
}
