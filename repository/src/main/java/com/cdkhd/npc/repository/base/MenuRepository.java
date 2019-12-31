package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Menu;

public interface MenuRepository extends BaseRepository<Menu> {
    Menu findByName(String name);
}
