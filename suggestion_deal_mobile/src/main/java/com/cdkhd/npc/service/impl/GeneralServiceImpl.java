package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionSetting;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.SuggestionSettingRepository;
import com.cdkhd.npc.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Service
public class GeneralServiceImpl implements GeneralService {
    private final SuggestionSettingRepository suggestionSettingRepository;
    private final SuggestionRepository suggestionRepository;

    @Autowired
    public GeneralServiceImpl(SuggestionSettingRepository suggestionSettingRepository, SuggestionRepository suggestionRepository) {
        this.suggestionSettingRepository = suggestionSettingRepository;
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    public void scanSuggestions(MobileUserDetailsImpl userDetails, Byte level) {
        Assert.notNull(level, "level cannot be null");

        SuggestionSetting suggestionSetting = null;
        if (level.equals(LevelEnum.TOWN.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
        }else if (level.equals(LevelEnum.AREA.getValue())){
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
        }
        if (suggestionSetting != null) {
            List<Suggestion> suggestions = suggestionRepository.findByExpectDateIsNotNullAndFinishTimeIsNullAndIsDelFalse();
            Date now = new Date();
            for (Suggestion suggestion : suggestions) {
                if (now.after(suggestion.getExpectDate())){//超期
                    suggestion.setExceedLimit(true);
                }else if ((suggestion.getExpectDate().getTime() - now.getTime())/ (1000L*3600L*24L) < suggestionSetting.getDeadline()){//临期
                    suggestion.setCloseDeadLine(true);
                }
            }
            suggestionRepository.saveAll(suggestions);
        }
    }
}
