package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Systems;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface SystemRepository extends BaseRepository<Systems> {

    List<Systems> findByEnabledTrue();

    Systems findByName(String name);

    Systems findByKeyword(String system);
}
