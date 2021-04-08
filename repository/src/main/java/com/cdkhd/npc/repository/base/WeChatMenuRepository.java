package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.WeChatMenu;

public interface WeChatMenuRepository extends BaseRepository<WeChatMenu> {
    WeChatMenu findByUniqueKey(String uniqueKey);
}
