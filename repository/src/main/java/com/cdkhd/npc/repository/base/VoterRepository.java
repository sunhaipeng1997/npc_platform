package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Voter;

public interface VoterRepository extends BaseRepository<Voter> {


    Voter findByAccountUid(String uid);
}
