package com.cdkhd.npc.repository.suggestion_deal;

import com.cdkhd.npc.entity.Unit;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface UnitRepository extends BaseRepository<Unit> {

    Unit findByNameAndLevelAndTownUidAndUidIsNotAndIsDelFalse(String name, Byte level, String townUid, String uid);

    Unit findByNameAndLevelAndAreaUidAndUidIsNotAndIsDelFalse(String name, Byte level, String areaUid, String uid);

    Unit findByNameAndLevelAndAreaUidAndIsDelFalse(String name, Byte value, String uid);

    Unit findByNameAndLevelAndTownUidAndIsDelFalse(String name, Byte value, String uid);

    List<Unit> findByLevelAndAreaUidAndStatusAndIsDelFalse(Byte value, String uid, Byte status);

    List<Unit> findByLevelAndTownUidAndStatusAndIsDelFalse(Byte value, String uid, Byte status);

}
