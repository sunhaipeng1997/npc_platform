package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.PerformanceImage;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface PerformanceImageRepository extends BaseRepository<PerformanceImage> {

    List<PerformanceImage> findByPerformanceUid(String uid);
}
