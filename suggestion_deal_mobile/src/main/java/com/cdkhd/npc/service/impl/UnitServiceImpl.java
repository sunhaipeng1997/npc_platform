package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.Unit;
import com.cdkhd.npc.entity.dto.LevelDto;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.suggestion_deal.UnitRepository;
import com.cdkhd.npc.service.UnitService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UnitServiceImpl implements UnitService {


    private UnitRepository unitRepository;


    @Autowired
    public UnitServiceImpl(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }




    @Override
    public RespBody unitList(MobileUserDetailsImpl userDetails, LevelDto levelDto) {
        RespBody body = new RespBody();
        List<Unit> units = Lists.newArrayList();
        if (levelDto.getLevel().equals(LevelEnum.AREA.getValue())) {
            units = unitRepository.findByLevelAndAreaUidAndStatusAndIsDelFalse(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (levelDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            units = unitRepository.findByLevelAndTownUidAndStatusAndIsDelFalse(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = units.stream().map(unit -> CommonVo.convert(unit.getUid(),unit.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

}
