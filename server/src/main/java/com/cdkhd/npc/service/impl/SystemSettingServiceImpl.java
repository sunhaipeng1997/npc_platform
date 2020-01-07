package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.SystemSetting;
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
    public SystemSetting getSystemSetting() {
        SystemSetting systemSetting = new SystemSetting();
        List<SystemSetting> systemSettings = systemSettingRepository.findAll();
        if (CollectionUtils.isNotEmpty(systemSettings)){
            systemSetting = systemSettings.get(0);
        }
        return systemSetting;
    }
}
