package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.service.MenuService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/menu")
public class MenuApi {

    private MenuService menuService;

    @Autowired
    public MenuApi(MenuService menuService) {
        this.menuService = menuService;
    }


    /**
     * 获取当前用户的菜单
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/getMenus")
    public ResponseEntity getMenus(@CurrentUser MobileUserDetailsImpl userDetails, String system, Byte level) {
        RespBody body = menuService.getMenus(userDetails, system, level);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取菜单未读数量
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/countUnRead")
    public ResponseEntity countUnRead(@CurrentUser MobileUserDetailsImpl userDetails, Byte level) {
        RespBody body = menuService.countUnRead(userDetails, level);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取当前身份
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/getLevels")
    public ResponseEntity getLevels(@CurrentUser MobileUserDetailsImpl userDetails, Byte level) {
        RespBody body = menuService.getLevels(userDetails, level);
        return ResponseEntity.ok(body);
    }


}
