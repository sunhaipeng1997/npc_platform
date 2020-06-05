package com.cdkhd.npc.repository.suggestion_deal;

import com.cdkhd.npc.entity.UnitImage;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface UnitImageRepository extends BaseRepository<UnitImage> {
    List<UnitImage> findByTypeAndBelongToId(Byte type, Long id);
}
