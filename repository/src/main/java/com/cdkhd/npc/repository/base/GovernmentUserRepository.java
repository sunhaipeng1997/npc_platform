package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.GovernmentUser;

public interface GovernmentUserRepository extends BaseRepository<GovernmentUser> {

    GovernmentUser findByAccountUsername(String username);

    GovernmentUser findByAccountMobile(String mobile);

    GovernmentUser findByAccountUid(String uid);
}
