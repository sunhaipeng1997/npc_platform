package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.entity.vo.RelationVo;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.RegisterService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegisterServiceImpl implements RegisterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServiceImpl.class);

    private AccountRepository accountRepository;

    private AreaRepository areaRepository;

    @Autowired
    public RegisterServiceImpl(AccountRepository accountRepository, AreaRepository areaRepository) {
        this.accountRepository = accountRepository;
        this.areaRepository = areaRepository;
    }


    @Override
    public RespBody getRelations() {
        RespBody body = new RespBody();
        List<Area> areas = areaRepository.findByStatus(StatusEnum.ENABLED.getValue());
        List<RelationVo> areaVos = Lists.newArrayList();
        for (Area area : areas) {
            RelationVo areaVo = RelationVo.convert(area.getUid(),area.getName());
            List<RelationVo> townVos = Lists.newArrayList();
            for (Town town : area.getTowns()) {
                if (town.getStatus().equals(StatusEnum.ENABLED.getValue())) {
                    RelationVo townVo = RelationVo.convert(town.getUid(), town.getName());
                    townVo.setChildren(town.getVillages().stream().map(village -> RelationVo.convert(village.getUid(), village.getName())).collect(Collectors.toList()));
                    townVos.add(townVo);
                }
            }
            areaVo.setChildren(townVos);
            areaVos.add(areaVo);
        }
        body.setData(areaVos);
        return body;
    }

    @Override
    public RespBody register(UserInfoDto userInfoDto) {
        RespBody body = new RespBody();

        return body;
    }
}
