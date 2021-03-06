package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.dto.SystemSettingDto;
import com.cdkhd.npc.entity.vo.SystemSettingVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemSettingServiceImpl implements SystemSettingService {
    private Environment env;

    private SystemSettingRepository systemSettingRepository;


    @Autowired
    public SystemSettingServiceImpl(Environment env, SystemSettingRepository systemSettingRepository) {
        this.env = env;
        this.systemSettingRepository = systemSettingRepository;
    }



    @Override
    public SystemSetting getSystemSetting(Byte level, String uid) {
        SystemSetting systemSetting = new SystemSetting();
        if (level.equals(LevelEnum.TOWN.getValue())){
            systemSetting = systemSettingRepository.findByLevelAndTownUid(level,uid);
        }else if (level.equals(LevelEnum.AREA.getValue())){
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(level,uid);
        }
        return systemSetting;
    }

    @Override
    public RespBody getSystemSettings(Byte level, String uid) {
        RespBody body = new RespBody();
        SystemSettingVo systemSettingVo = SystemSettingVo.convert(this.getSystemSetting(level,uid));
        body.setData(systemSettingVo);
        return body;
    }

    @Override
    public RespBody saveSystemSetting(SystemSettingDto systemSettingDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(systemSettingDto.getUid())){
            body.setMessage("???????????????????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        SystemSetting systemSetting = systemSettingRepository.findByUid(systemSettingDto.getUid());
        if (systemSettingDto.getPerformanceGroupAudit() != null){
            systemSetting.setPerformanceGroupAudit(systemSettingDto.getPerformanceGroupAudit());
        }else if (systemSettingDto.getShowSubPerformance() != null){
            systemSetting.setShowSubPerformance(systemSettingDto.getShowSubPerformance());
            systemSetting.setVoterOpinionToAll(systemSettingDto.getVoterOpinionToAll());
            systemSetting.setMemberOpinionToMember(systemSettingDto.getMemberOpinionToMember());
            systemSetting.setFloatNotice(systemSettingDto.getFloatNotice());
            systemSetting.setPushNews(systemSettingDto.getPushNews());
            systemSetting.setPushStudy(systemSettingDto.getPushStudy());
            systemSetting.setPushUpdate(systemSettingDto.getPushUpdate());
            systemSetting.setQuickWork(systemSettingDto.getQuickWork());
        }
        systemSettingRepository.saveAndFlush(systemSetting);
        return body;
    }
}
