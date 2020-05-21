package com.cdkhd.npc.repository.suggestion_deal;
/*
 * @description:政府模块持久层
 * @author:liyang
 * @create:2020-05-20
 */

import com.cdkhd.npc.entity.Government;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface GovernmentRepository extends BaseRepository<Government> {

    Government findByAreaUidAndName(String areaUid, String name);

    Government findByAreaUidAndLevel(String areaUid, Byte level);

    Government findByTownUid(String townUid);
}
