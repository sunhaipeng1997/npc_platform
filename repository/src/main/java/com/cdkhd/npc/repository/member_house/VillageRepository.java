package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Village;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface VillageRepository extends BaseRepository<Village> {

    List<Village> findByTownUidAndNpcMemberGroupIsNull(String townUid);
}
