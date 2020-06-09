package com.cdkhd.npc.repository.suggestion_deal;

import com.cdkhd.npc.entity.HandleProcess;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface HandleProcessRepository extends BaseRepository<HandleProcess> {
    HandleProcess findByUid(String uid);
}
