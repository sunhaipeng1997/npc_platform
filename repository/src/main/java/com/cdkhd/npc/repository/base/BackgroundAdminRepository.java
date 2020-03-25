package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.BackgroundAdmin;

public interface BackgroundAdminRepository extends BaseRepository<BackgroundAdmin> {

    BackgroundAdmin findByAccountMobile(String mobile);
}
