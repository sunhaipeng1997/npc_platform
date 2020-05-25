package com.cdkhd.npc.repository.suggestion_deal;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface ConveyProcessRepository extends BaseRepository<ConveyProcess> {
    ConveyProcess findByUid(String uid);
}
