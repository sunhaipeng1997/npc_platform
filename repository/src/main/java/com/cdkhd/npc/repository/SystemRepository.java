package com.cdkhd.npc.repository;

import com.cdkhd.npc.entity.Systems;

import java.util.List;

public interface SystemRepository extends BaseRepository<Systems> {

    List<Systems> findByEnabledTrue();
}
