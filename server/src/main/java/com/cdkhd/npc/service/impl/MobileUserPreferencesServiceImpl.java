package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.LoginUP;
import com.cdkhd.npc.entity.MobileUserPreferences;
import com.cdkhd.npc.entity.dto.MobileUserPreferencesDto;
import com.cdkhd.npc.entity.vo.MobileUserPreferencesVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.ShortcutActionEnum;
import com.cdkhd.npc.repository.base.LoginUPRepository;
import com.cdkhd.npc.repository.base.MobileUserPreferencesRepository;
import com.cdkhd.npc.service.MobileUserPreferencesService;
import com.cdkhd.npc.vo.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MobileUserPreferencesServiceImpl implements MobileUserPreferencesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileUserPreferencesServiceImpl.class);

    private MobileUserPreferencesRepository mobileUserPreferencesRepository;
    private LoginUPRepository loginUPRepository;

    @Autowired
    public MobileUserPreferencesServiceImpl(MobileUserPreferencesRepository mobileUserPreferencesRepository, LoginUPRepository loginUPRepository) {
        this.mobileUserPreferencesRepository = mobileUserPreferencesRepository;
        this.loginUPRepository = loginUPRepository;
    }

    @Override
    public RespBody getMobileUserPreferences(UserDetailsImpl userDetails){
        RespBody<MobileUserPreferencesVo> body = new RespBody<>();

        LoginUP loginUP = loginUPRepository.findByUsername(userDetails.getUsername());
        if(loginUP == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("无此用户名");
            LOGGER.error("查询用户偏好设置失败，无此用户名");
            return body;
        }

        MobileUserPreferences mobileUserPreferences =  mobileUserPreferencesRepository.findByAccountId(loginUP.getAccount().getId());
        if(mobileUserPreferences == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("无此用户偏好设置");
            LOGGER.error("查询用户偏好设置失败，无此用户偏好设置");
            return body;
        }
        MobileUserPreferencesVo vo = MobileUserPreferencesVo.convert(mobileUserPreferences);

        List<String> actionList = new ArrayList<>();
        for(ShortcutActionEnum UserPref :ShortcutActionEnum.values()){
            switch (UserPref){
                case CLOSE:
                    actionList.add(UserPref.getName());
                    break;
                case GIVE_ADVICE:
                    if(userDetails.getRoles().contains(AccountRoleEnum.VOTER.getName())){
                        actionList.add(UserPref.getName());
                    }
                    break;
                case MAKE_SUGGESTION:
                    if(userDetails.getRoles().contains(AccountRoleEnum.NPC_MEMBER.getName())){
                        actionList.add(UserPref.getName());
                    }
                    break;
                case ADD_PERFORMANCE:
                    if(userDetails.getRoles().contains(AccountRoleEnum.NPC_MEMBER.getName())){
                        actionList.add(UserPref.getName());
                    }
                    break;
                case SCAN_QR_CODE:
                    actionList.add(UserPref.getName());
                    break;
            }

        }
        vo.setActionList(actionList);

        body.setData(vo);
        return body;
    }


    @Override
    public RespBody updateMobileUserPreferences(UserDetailsImpl userDetails, MobileUserPreferencesDto dto){
        RespBody body = new RespBody();

        LoginUP loginUP = loginUPRepository.findByUsername(userDetails.getUsername());
        if(loginUP == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("无此用户名");
            LOGGER.error("更新用户偏好设置失败，无此用户名");
            return body;
        }

        MobileUserPreferences mobileUserPreferences =  mobileUserPreferencesRepository.findByAccountId(loginUP.getAccount().getId());
        if(mobileUserPreferences == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("无此用户偏好设置");
            LOGGER.error("更新用户偏好设置失败，无此用户偏好设置");
            return body;
        }

        if(!dto.getShortcutAction().isEmpty()){
            mobileUserPreferences.setShortcutAction(dto.getShortcutAction());
        }

        if(!dto.getNewsStyle().isEmpty()){
            mobileUserPreferences.setNewsStyle(dto.getNewsStyle());
        }

        mobileUserPreferencesRepository.save(mobileUserPreferences);

        body.setMessage("更新用户偏好设置成功");
        return body;
    }
}
