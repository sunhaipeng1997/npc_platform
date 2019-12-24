package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Performance;
import com.cdkhd.npc.entity.PerformanceType;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface PerformanceRepository extends BaseRepository<Performance> {

    List<Performance> findByTypeUid();
}
