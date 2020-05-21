package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.SuggestionSetting;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.persistence.Column;

@Getter
@Setter
public class SugSettingVo extends BaseVo {

    //办理期限（单位：天）
    @Column(nullable = false)
    private Integer deadline;

    //催办频率（单位：天）
    @Column(nullable = false)
    private Integer urgeFre;

    public static SugSettingVo convert(SuggestionSetting suggestionSetting) {
        SugSettingVo vo = new SugSettingVo();
        BeanUtils.copyProperties(suggestionSetting,vo);
        return vo;
    }
}
