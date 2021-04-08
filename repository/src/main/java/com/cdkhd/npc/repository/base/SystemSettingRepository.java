package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.SystemSetting;

public interface SystemSettingRepository extends BaseRepository<SystemSetting> {

    SystemSetting findByLevelAndTownUid(Byte level, String uid);

    SystemSetting findByLevelAndAreaUid(Byte level, String uid);
}
