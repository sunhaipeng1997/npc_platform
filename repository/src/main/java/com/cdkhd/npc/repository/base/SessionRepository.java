package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Session;

import java.util.List;

public interface SessionRepository extends BaseRepository<Session> {
    List<Session> findByTownUid(String townUid);

    List<Session> findByAreaUidAndLevel(String townUid, Byte level);
}
