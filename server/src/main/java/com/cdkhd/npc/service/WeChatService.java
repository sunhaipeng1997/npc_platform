package com.cdkhd.npc.service;

import java.util.Map;

public interface WeChatService {
    Map<String, String> getConfig4JsApi(String jsApiUrl);
}
