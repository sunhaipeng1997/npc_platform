package com.cdkhd.npc.repository.suggestion_deal;
/*
 * @description:附议模块持久层
 * @author:liyang
 * @create:2020-05-28
 */

import com.cdkhd.npc.entity.Seconded;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface SecondedRepository extends BaseRepository<Seconded> {

    List<Seconded> findByNpcMemberUid(String uid);

}
