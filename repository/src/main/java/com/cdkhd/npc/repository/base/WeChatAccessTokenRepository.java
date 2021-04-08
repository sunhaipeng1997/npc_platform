package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.WeChatAccessToken;

public interface WeChatAccessTokenRepository extends BaseRepository<WeChatAccessToken> {
    WeChatAccessToken findByAppid(String appid);
}
