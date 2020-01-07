package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.LoginUP;

public interface LoginUPRepository extends BaseRepository<LoginUP>{
    LoginUP findByUsername(String username);
}
