package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Area;

import java.util.List;

public interface AreaRepository extends BaseRepository<Area> {

    Area findByName(String areaName);

    List<Area> findByStatus(Byte value);
}
