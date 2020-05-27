package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.SuggestionSetting;
import com.cdkhd.npc.entity.dto.SugSettingDto;
import com.cdkhd.npc.entity.vo.SugSettingVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.suggestion_deal.SuggestionSettingRepository;
import com.cdkhd.npc.service.SugSettingService;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SugSettingServiceImpl implements SugSettingService {
    private Environment env;

    private SuggestionSettingRepository suggestionSettingRepository;

    @Autowired
    public SugSettingServiceImpl(Environment env, SuggestionSettingRepository suggestionSettingRepository) {
        this.env = env;
        this.suggestionSettingRepository = suggestionSettingRepository;
    }

    public SuggestionSetting getSugSetting(Byte level, String uid) {
        SuggestionSetting suggestionSetting = null;
        if (level.equals(LevelEnum.AREA.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(level,uid);
        }else if(level.equals(LevelEnum.TOWN.getValue())){
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(level,uid);
        }
        return suggestionSetting;
    }

    @Override
    public RespBody getSugSettings(Byte level, String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setMessage("找不到建议设置！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        SuggestionSetting suggestionSetting = this.getSugSetting(level,uid);
        if (suggestionSetting != null) {
            SugSettingVo sugSettingVo = SugSettingVo.convert(suggestionSetting);
            body.setData(sugSettingVo);
        }
        return body;
    }

    @Override
    public RespBody saveSugSetting(UserDetailsImpl userDetails, SugSettingDto sugSettingDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(sugSettingDto.getUid())){
            body.setMessage("保存系统设置失败！");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        SuggestionSetting suggestionSetting = suggestionSettingRepository.findByUid(sugSettingDto.getUid());
        suggestionSetting.setExpectDate(sugSettingDto.getExpectDate());//默认建议办理周期
        suggestionSetting.setDeadline(sugSettingDto.getDeadline());//默认建议办理周期
        suggestionSetting.setUrgeFre(sugSettingDto.getUrgeFre());//默认催办频率
        suggestionSettingRepository.saveAndFlush(suggestionSetting);
        return body;
    }
}
