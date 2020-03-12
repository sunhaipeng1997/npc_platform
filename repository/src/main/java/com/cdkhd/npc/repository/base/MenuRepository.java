package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Menu;

import java.util.List;
import java.util.Set;

public interface MenuRepository extends BaseRepository<Menu> {
    Menu findByName(String name);

    Set<Menu> findMenusByName(String name);

    List<Menu> findBySystemsUidAndEnabled(String system, byte value);
}
