package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.MobileUserPreferences;
import com.cdkhd.npc.entity.dto.MobileUserPreferencesDto;
import com.cdkhd.npc.entity.vo.MobileUserPreferencesVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.NewsStyleEnum;
import com.cdkhd.npc.enums.ShortcutActionEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.LoginUPRepository;
import com.cdkhd.npc.repository.base.MobileUserPreferencesRepository;
import com.cdkhd.npc.service.MobileUserPreferencesService;
import com.cdkhd.npc.vo.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class MobileUserPreferencesServiceImpl implements MobileUserPreferencesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileUserPreferencesServiceImpl.class);

    private MobileUserPreferencesRepository mobileUserPreferencesRepository;
    private AccountRepository accountRepository;
    private LoginUPRepository loginUPRepository;


    @Autowired
    public MobileUserPreferencesServiceImpl(MobileUserPreferencesRepository mobileUserPreferencesRepository, AccountRepository accountRepository, LoginUPRepository loginUPRepository) {
        this.mobileUserPreferencesRepository = mobileUserPreferencesRepository;
        this.accountRepository = accountRepository;
        this.loginUPRepository = loginUPRepository;
    }


    @Override
    public RespBody getMobileUserPreferences(UserDetailsImpl userDetails){
        RespBody<MobileUserPreferencesVo> body = new RespBody<>();

        Account account =  accountRepository.findByUid(userDetails.getUid());
        if(account == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("????????????");
            LOGGER.error("?????????????????????????????????????????????");
            return body;
        }

        MobileUserPreferences mobileUserPreferences =  mobileUserPreferencesRepository.findByAccountId(account.getId());
        if(mobileUserPreferences == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("????????????????????????");
            LOGGER.error("?????????????????????????????????????????????????????????");
            return body;
        }
        MobileUserPreferencesVo vo = MobileUserPreferencesVo.convert(mobileUserPreferences);

        //?????????????????????????????????????????????????????????
        List<String> actionList = new ArrayList<>();
        for(ShortcutActionEnum UserPref : ShortcutActionEnum.values()){
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

        //???????????????????????????
        List<String> newsStyleList = new ArrayList<>();
        for(NewsStyleEnum newsStyle : NewsStyleEnum.values()){
            newsStyleList.add(newsStyle.getName());
        }
        vo.setNewsStyleList(newsStyleList);

        body.setData(vo);
        return body;
    }


    @Override
    public RespBody updateMobileUserPreferences(UserDetailsImpl userDetails, MobileUserPreferencesDto dto){
        RespBody body = new RespBody();

        Account account =  loginUPRepository.findByUsername(userDetails.getUsername()).getAccount();
        if(account == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("????????????");
            LOGGER.error("?????????????????????????????????????????????");
            return body;
        }

        MobileUserPreferences mobileUserPreferences =  mobileUserPreferencesRepository.findByAccountId(account.getId());
        if(mobileUserPreferences == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("????????????????????????");
            LOGGER.error("?????????????????????????????????????????????????????????");
            return body;
        }

        if(!dto.getShortcutAction().isEmpty()){
            mobileUserPreferences.setShortcutAction(dto.getShortcutAction());
        }

        if(!dto.getNewsStyle().isEmpty()){
            mobileUserPreferences.setNewsStyle(dto.getNewsStyle());
        }

        mobileUserPreferencesRepository.save(mobileUserPreferences);

        body.setMessage("??????????????????????????????");
        return body;
    }
}
