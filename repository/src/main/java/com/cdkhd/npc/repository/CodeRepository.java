package com.cdkhd.npc.repository;

import com.cdkhd.npc.entity.Code;

public interface CodeRepository extends BaseRepository<Code> {
    Code findByMobile(String mobile);
}
