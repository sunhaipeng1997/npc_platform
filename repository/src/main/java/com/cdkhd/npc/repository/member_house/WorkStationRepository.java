package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.WorkStation;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface WorkStationRepository extends BaseRepository<WorkStation> {

    WorkStation findByAreaUidAndName(String areaUid, String name);

    WorkStation findByTownUidAndName(String townUid, String name);

    WorkStation findByAreaUidAndNameAndUidIsNot(String areaUid, String name, String uid);

    WorkStation findByTownUidAndNameAndUidIsNot(String townUid, String name, String uid);
}
