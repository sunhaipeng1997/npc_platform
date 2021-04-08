package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.service.IndexService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal_mobile/index")
public class IndexApi {
    private IndexService indexService;

    @Autowired
    public IndexApi(IndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping("/levels")
    public ResponseEntity getLevels(@CurrentUser MobileUserDetailsImpl userDetails) {
        RespBody body = indexService.getIdentityInfo(userDetails);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/menus")
    public ResponseEntity getMenus(@CurrentUser MobileUserDetailsImpl userDetails, Byte role, Byte level) {
        RespBody body = indexService.getMenus(userDetails, role, level);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/countUnRead")
    public ResponseEntity countUnRead(@CurrentUser MobileUserDetailsImpl userDetails, Byte level){
        RespBody body = indexService.countUnRead(userDetails, level);
        return ResponseEntity.ok(body);
    }
}
