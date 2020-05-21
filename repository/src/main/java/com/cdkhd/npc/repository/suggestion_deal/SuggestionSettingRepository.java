package com.cdkhd.npc.repository.suggestion_deal;

import com.cdkhd.npc.entity.SuggestionSetting;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface SuggestionSettingRepository extends BaseRepository<SuggestionSetting> {


    SuggestionSetting findByLevelAndAreaUid(Byte level, String uid);

    SuggestionSetting findByLevelAndTownUid(Byte level, String uid);
}
