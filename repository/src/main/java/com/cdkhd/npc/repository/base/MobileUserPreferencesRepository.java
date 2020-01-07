package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.MobileUserPreferences;

public interface MobileUserPreferencesRepository extends BaseRepository<MobileUserPreferences>{

    MobileUserPreferences findByAccountId(Long id);
}
