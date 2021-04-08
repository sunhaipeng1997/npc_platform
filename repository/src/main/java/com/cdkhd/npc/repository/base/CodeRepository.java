package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.Code;
import com.cdkhd.npc.repository.base.BaseRepository;

public interface CodeRepository extends BaseRepository<Code> {
    Code findByMobile(String mobile);
}
