package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Town;

import java.util.List;

public interface TownRepository extends BaseRepository<Town> {

    Town findByAreaUidAndName(String areaUid, String townName);

    List<Town> findByAreaUidOrderByNameAsc(String uid);

    Town findByAreaUidAndNameAndUidIsNot(String areaUid, String name, String uid);

    List<Town> findByAreaUidAndIsDelFalse(String areaUid);

}
