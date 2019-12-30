package com.cdkhd.npc.repository.member_house;

import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.repository.base.BaseRepository;

import java.util.List;

public interface OpinionRepository extends BaseRepository<Opinion> {


    List<Opinion> findBySenderUid(String uid);
}
