package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Performance;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface PerformanceRepository extends BaseRepository<Performance> {

    Performance findByTransUid(String transUid);

    Performance findByUidAndTransUid(String uid, String transUid);
}
