package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.CommonDict;

import java.util.List;

public interface CommonDictRepository extends BaseRepository<CommonDict> {
    List<CommonDict> findByTypeAndIsDelFalse(String type);
}
