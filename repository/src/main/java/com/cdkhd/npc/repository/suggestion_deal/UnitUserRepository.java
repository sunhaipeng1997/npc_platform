package com.cdkhd.npc.repository.suggestion_deal;

import com.cdkhd.npc.entity.SuggestionSetting;
import com.cdkhd.npc.entity.Unit;
import com.cdkhd.npc.entity.UnitUser;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface UnitUserRepository extends BaseRepository<UnitUser> {

    UnitUser findByMobileAndUidIsNotAndIsDelFalse(String mobile, String uid);

    UnitUser findByMobileAndIsDelFalse(String mobile);

}
