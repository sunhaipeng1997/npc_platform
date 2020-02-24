package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {
    private Environment env;

    private SystemSettingRepository systemSettingRepository;


    @Autowired
    public SystemSettingServiceImpl(Environment env, SystemSettingRepository systemSettingRepository) {
        this.env = env;
        this.systemSettingRepository = systemSettingRepository;
    }



    @Override
    public SystemSetting getSystemSetting(UserDetailsImpl userDetails) {
        SystemSetting systemSetting = new SystemSetting();
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
            systemSetting = systemSettingRepository.findByLevelAndTownUid(userDetails.getLevel(),userDetails.getTown().getUid());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(userDetails.getLevel(),userDetails.getArea().getUid());
        }
        return systemSetting;
    }
}
