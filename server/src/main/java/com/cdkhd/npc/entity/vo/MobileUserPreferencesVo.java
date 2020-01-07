package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.MobileUserPreferences;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Getter
@Setter
public class MobileUserPreferencesVo extends BaseVo {

    private String shortcutAction;

    private String newsStyle;

    private List<String> actionList;

    private List<String> newsStyleList;

    public static MobileUserPreferencesVo convert(MobileUserPreferences mobileUserPreferences) {
        MobileUserPreferencesVo vo = new MobileUserPreferencesVo();

        BeanUtils.copyProperties(mobileUserPreferences, vo);

        return vo;
    }
}
