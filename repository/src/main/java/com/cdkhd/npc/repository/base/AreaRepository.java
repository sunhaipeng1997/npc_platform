package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Area;

public interface AreaRepository extends BaseRepository<Area> {

    Area findByName(String areaName);
}
