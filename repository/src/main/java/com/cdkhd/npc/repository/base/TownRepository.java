package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Town;

import java.util.List;

public interface TownRepository extends BaseRepository<Town> {
    List<Town> findByAreaUid(String areaUid);
}
