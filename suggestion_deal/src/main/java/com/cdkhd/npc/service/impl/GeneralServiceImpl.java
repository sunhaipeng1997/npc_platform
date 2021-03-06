package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.entity.SuggestionSetting;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.SuggestionSettingRepository;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
//@Transactional
public class GeneralServiceImpl implements GeneralService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralServiceImpl.class);

    private SuggestionBusinessRepository suggestionBusinessRepository;
    private TownRepository townRepository;
    private SuggestionSettingRepository suggestionSettingRepository;
    private SuggestionRepository suggestionRepository;

    @Autowired
    public GeneralServiceImpl(SuggestionBusinessRepository suggestionBusinessRepository, TownRepository townRepository, SuggestionSettingRepository suggestionSettingRepository, SuggestionRepository suggestionRepository) {
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.townRepository = townRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
        this.suggestionRepository = suggestionRepository;
    }

    /**
     * ???????????????????????????????????????
     * @param userDetails
     * @return
     */
    @Override
    public RespBody findSugBusiness(UserDetailsImpl userDetails) {
        RespBody<List<CommonVo>> body = new RespBody<>();
        List<SuggestionBusiness> sb = Lists.newArrayList();
        //??????????????????????????????????????????
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))){
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(), userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    /**
     * ???????????????????????????
     * @param townUid
     * @return
     */
    @Override
    public RespBody findSugBusinessByTown(String townUid) {
        RespBody<List<CommonVo>> body = new RespBody<>();
        List<SuggestionBusiness> suggestionBusinesses = Lists.newArrayList();
        Town town = townRepository.findByUid(townUid);
        if (town != null && town.getType().equals(LevelEnum.AREA.getValue())){//???????????????????????????????????????????????????
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), town.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (StringUtils.isNotEmpty(townUid)){//?????????
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(), townUid, StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = suggestionBusinesses.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public void scanSuggestions(UserDetailsImpl userDetails) {
        SuggestionSetting suggestionSetting = null;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
        }
        if (suggestionSetting != null) {
            List<Suggestion> suggestions = suggestionRepository.findByExpectDateIsNotNullAndFinishTimeIsNullAndIsDelFalse();
            Date now = new Date();
            for (Suggestion suggestion : suggestions) {
                if (now.after(suggestion.getExpectDate())){//??????
                    suggestion.setExceedLimit(true);
                }else if ((suggestion.getExpectDate().getTime() - now.getTime())/ (1000L*3600L*24L) < suggestionSetting.getDeadline()){//??????
                    suggestion.setCloseDeadLine(true);
                }
            }
            suggestionRepository.saveAll(suggestions);
        }
    }
}
