package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.*;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.base.DelaySuggestionRepository;
import com.cdkhd.npc.repository.base.GovernmentUserRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.SuggestionSettingRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitRepository;
import com.cdkhd.npc.repository.suggestion_deal.UrgeRepository;
import com.cdkhd.npc.service.GovSuggestionService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GovSuggestionServiceImpl implements GovSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovSuggestionServiceImpl.class);

    private SuggestionRepository suggestionRepository;

    private UnitRepository unitRepository;

    private GovernmentUserRepository governmentUserRepository;

    private ConveyProcessRepository conveyProcessRepository;

    private DelaySuggestionRepository delaySuggestionRepository;

    private UrgeRepository urgeRepository;

    private SuggestionSettingRepository suggestionSettingRepository;

    private AccountRepository accountRepository;

    @Autowired
    public GovSuggestionServiceImpl(SuggestionRepository suggestionRepository, UnitRepository unitRepository, GovernmentUserRepository governmentUserRepository, ConveyProcessRepository conveyProcessRepository, DelaySuggestionRepository delaySuggestionRepository, UrgeRepository urgeRepository, SuggestionSettingRepository suggestionSettingRepository, AccountRepository accountRepository) {
        this.suggestionRepository = suggestionRepository;
        this.unitRepository = unitRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.conveyProcessRepository = conveyProcessRepository;
        this.delaySuggestionRepository = delaySuggestionRepository;
        this.urgeRepository = urgeRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public RespBody getGovSuggestion(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = new RespBody();
        //???????????????????????????????????????????????????
        int begin = govSuggestionPageDto.getPage() - 1;
        Sort.Order urge = new Sort.Order(Sort.Direction.DESC, "urge");//??????
        Sort.Order urgeLevel = new Sort.Order(Sort.Direction.DESC, "urgeLevel");//??????
        Sort.Order exceedLimit = new Sort.Order(Sort.Direction.DESC, "exceedLimit");//??????
        Sort.Order closeDeadLine = new Sort.Order(Sort.Direction.DESC, "closeDeadLine");//??????
        Sort.Order viewSort = new Sort.Order(Sort.Direction.ASC, "govView");//????????????????????????
        Sort.Order statusSort = new Sort.Order(Sort.Direction.ASC, "status");//??????????????????
        Sort.Order createAt = new Sort.Order(Sort.Direction.DESC, "createTime");//????????????????????????
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(urge);
        orders.add(urgeLevel);
        orders.add(exceedLimit);
        orders.add(closeDeadLine);
        orders.add(viewSort);
        orders.add(statusSort);
        orders.add(createAt);
        Sort sort = Sort.by(orders);
        Pageable page = PageRequest.of(begin, govSuggestionPageDto.getSize(), sort);
        Page<Suggestion> suggestionPage = this.getSuggestionPage(userDetails, govSuggestionPageDto, page);
        PageVo<GovSugListVo> vo = new PageVo<>(suggestionPage, govSuggestionPageDto);
        List<GovSugListVo> suggestionVos = suggestionPage.getContent().stream().map(suggestion -> GovSugListVo.convert(suggestion,govSuggestionPageDto.getSearchType())).collect(Collectors.toList());
        vo.setContent(suggestionVos);
        body.setData(vo);
        return body;
    }

    private Page<Suggestion> getSuggestionPage(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, Pageable page) {
        Page<Suggestion> suggestionPage = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), govSuggestionPageDto.getLevel()));//?????????????????????????????????????????????
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (govSuggestionPageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            if (null != govSuggestionPageDto.getSearchType()) {
                if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.WAIT_DEAL_SUG.getValue())){//??????????????????
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//????????????????????????
                    Join<Suggestion, ConveyProcess> join = root.join("conveyProcesses", JoinType.LEFT);
                    predicates.add(cb.isNull(join));//?????????????????????
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.ADJUST_UNIT_SUG.getValue())) { //???????????????????????????
                    Join<Suggestion, ConveyProcess> join = root.join("conveyProcesses", JoinType.LEFT);
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//????????????????????????
                    predicates.add(cb.equal(join.get("status").as(Byte.class), ConveyStatusEnum.CONVEY_FAILED.getValue()));//????????????????????????,????????????
                    predicates.add(cb.equal(join.get("dealStatus").as(Byte.class), GovDealStatusEnum.NOT_DEAL.getValue()));//????????????????????????????????????
                    predicates.add(cb.isFalse(join.get("dealDone").as(Boolean.class)));//???????????????????????????
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.APPLY_DELAY_SUG.getValue())) { //?????????????????????
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));//????????????????????????
                    Join<Suggestion, DelaySuggestion> join = root.join("delaySuggestions", JoinType.LEFT);
                    predicates.add(cb.isNull(join.get("accept")));//??????????????????????????????
                    predicates.add(cb.equal(join.get("delayTimes").as(Integer.class), root.get("delayTimes").as(Integer.class)));//????????????????????????????????????????????????
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.DEALING_SUG.getValue())) {//??????????????????
                    Predicate or = cb.or(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.TRANSFERRED_UNIT.getValue()),cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));
                    predicates.add(or);//???????????????????????????
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.FINISH_SUG.getValue())) {//??????????????????
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));//??????????????????????????????
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.ACCOMPLISHED_SUG.getValue())) {//??????????????????
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));//?????????????????????
                }
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return suggestionPage;
    }


    @Override
    public RespBody conveySuggestion(MobileUserDetailsImpl userDetails, ConveySuggestionDto conveySuggestionDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(conveySuggestionDto.getMainUnit()) || StringUtils.isEmpty(conveySuggestionDto.getUid())) {
            String message = "???????????????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(conveySuggestionDto.getUid());
        suggestion.setStatus(SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());
        suggestion.setConveyTime(new Date());
        Integer conveyTimes = suggestion.getConveyTimes()==null?1:suggestion.getConveyTimes() + 1;//????????????
        suggestion.setConveyTimes(conveyTimes);
        suggestion.setUnit(unitRepository.findByUid(conveySuggestionDto.getMainUnit()));
        SuggestionSetting suggestionSetting = null;
        if (conveySuggestionDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid());
        }else if (conveySuggestionDto.getLevel().equals(LevelEnum.AREA.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(),userDetails.getArea().getUid());
        }
        if (suggestionSetting != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, suggestionSetting.getExpectDate());
            suggestion.setExpectDate(calendar.getTime());
        }
        //???????????????
        suggestion.setGovernmentUser(governmentUserRepository.findByAccountUid(userDetails.getUid()));
        suggestionRepository.saveAndFlush(suggestion);
        //??????????????????????????????????????????
        this.govConvey(suggestion, conveySuggestionDto, userDetails.getUid(), conveyTimes);
        return body;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param suggestion
     * @param conveySuggestionDto
     * @param uid
     * @param conveyTimes
     */
    private void govConvey(Suggestion suggestion, ConveySuggestionDto conveySuggestionDto, String uid, Integer conveyTimes) {
        Set<ConveyProcess> conveyProcessList = Sets.newHashSet();
        //????????????
        if (conveySuggestionDto.getMainUnit() != null) {
            ConveyProcess conveyProcess = new ConveyProcess();
            conveyProcess.setGovernmentUser(governmentUserRepository.findByAccountUid(uid));
            conveyProcess.setUnit(unitRepository.findByUid(conveySuggestionDto.getMainUnit()));
            conveyProcess.setType(UnitTypeEnum.MAIN_UNIT.getValue());
            conveyProcess.setConveyTimes(conveyTimes);
            conveyProcess.setSuggestion(suggestion);
            conveyProcessList.add(conveyProcess);
        }
        //????????????
        if (CollectionUtils.isNotEmpty(conveySuggestionDto.getCoUnits())) {
            for (String coUnit : conveySuggestionDto.getCoUnits()) {
                ConveyProcess coUnitConvey = new ConveyProcess();
                coUnitConvey.setGovernmentUser(governmentUserRepository.findByAccountUid(uid));
                coUnitConvey.setUnit(unitRepository.findByUid(coUnit));
                coUnitConvey.setConveyTimes(conveyTimes);
                coUnitConvey.setType(UnitTypeEnum.CO_UNIT.getValue());
                coUnitConvey.setSuggestion(suggestion);
                conveyProcessList.add(coUnitConvey);
            }
        }
        if (CollectionUtils.isNotEmpty(conveyProcessList)) {
            conveyProcessRepository.saveAll(conveyProcessList);
        }
    }

    @Override
    public RespBody delaySuggestion(MobileUserDetailsImpl userDetails, DelaySuggestionDto delaySuggestionDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(delaySuggestionDto.getUid()) || delaySuggestionDto.getDelayDate() == null || delaySuggestionDto.getResult() == null) {
            String message = "???????????????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        DelaySuggestion delaySuggestion = delaySuggestionRepository.findByUid(delaySuggestionDto.getUid());
        if (delaySuggestion == null) {
            String message = "????????????????????????????????????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        delaySuggestion.setAccept(delaySuggestionDto.getResult());
        delaySuggestion.setRemark(delaySuggestionDto.getRemark());//????????????
        if (delaySuggestionDto.getResult()) {//?????????????????????????????????
            delaySuggestion.setDelayTime(delaySuggestionDto.getDelayDate());//??????????????????
            Suggestion suggestion = delaySuggestion.getSuggestion();//???????????????
            UnitSuggestion unitSuggestion = delaySuggestion.getUnitSuggestion();//????????????
            if (delaySuggestionDto.getResult()) {
                suggestion.setDelayTimes(suggestion.getDelayTimes() == null ? 1 : suggestion.getDelayTimes() + 1);
                suggestion.setExpectDate(delaySuggestionDto.getDelayDate());
                delaySuggestion.setSuggestion(suggestion);
                unitSuggestion.setDelayTimes(unitSuggestion.getDelayTimes() == null ? 1 : unitSuggestion.getDelayTimes() + 1);
                unitSuggestion.setExpectDate(delaySuggestionDto.getDelayDate());
                delaySuggestion.setUnitSuggestion(unitSuggestion);
            }
        }
        delaySuggestionRepository.saveAndFlush(delaySuggestion);
        return body;
    }

    @Override
    public RespBody adjustConvey(MobileUserDetailsImpl userDetails, AdjustConveyDto adjustConveyDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(adjustConveyDto.getUid()) || adjustConveyDto.getUnitType() == null) {
            String message = "???????????????????????????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.MAIN_UNIT.getValue()) && adjustConveyDto.getDealStatus().equals(GovDealStatusEnum.NOT_NEED_CONVEY.getValue())) {
            String message = "????????????????????????,????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.MAIN_UNIT.getValue()) && StringUtils.isEmpty(adjustConveyDto.getUnit())) {
            String message = "????????????????????????,????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        //???????????????????????????
        ConveyProcess conveyProcess = conveyProcessRepository.findByUid(adjustConveyDto.getUid());
        Suggestion suggestion = conveyProcess.getSuggestion();
        for (ConveyProcess process : suggestion.getConveyProcesses()) {
            if (!process.getUid().equals(adjustConveyDto.getUid()) && process.getUnit().getUid().equals(adjustConveyDto.getUnit()) && !process.getDealDone() && !process.getStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue())){
                String message = "?????????????????????????????????,??????????????????????????????";
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage(message);
                return body;
            }
        }
//        conveyProcess.setStatus(ConveyStatusEnum.CONVEYING.getValue());
        conveyProcess.setDealStatus(adjustConveyDto.getDealStatus());
        conveyProcess.setGovView(true);
        conveyProcess.setDealDone(true);
        conveyProcess.setDescription(adjustConveyDto.getDesc());
        conveyProcessRepository.saveAndFlush(conveyProcess);
        //????????????????????????????????????
        if (adjustConveyDto.getDealStatus().equals(GovDealStatusEnum.RE_CONVEY.getValue()) && StringUtils.isNotEmpty(adjustConveyDto.getUnit())) {//????????????????????????????????????????????????????????????????????????????????????
            Boolean acceptAll = true;//???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            Boolean isDealing = true;//????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            for (ConveyProcess process : suggestion.getConveyProcesses()) {
                if (!process.getDealDone() && process.getStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue())){//???????????????????????????????????????????????????????????????????????????????????????
                    //?????????????????????????????????????????????????????????????????????????????????????????????
                    acceptAll = false;
                }
                //???????????????????????????????????????????????????????????????????????????
                if (adjustConveyDto.getDealStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue()) && !process.getDealDone()){
                    isDealing = false;
                }
            }
            if (acceptAll) {
                if (isDealing && adjustConveyDto.getDealStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue())){//?????????????????????????????????
                    suggestion.setStatus(SuggestionStatusEnum.HANDLING.getValue());//?????????????????????????????????????????????????????????????????????
                }else {
                    suggestion.setStatus(SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());//???????????????????????????????????????????????????????????????
                }
            }
            suggestion.setConveyTime(new Date());
            Integer conveyTimes = suggestion.getConveyTimes() + 1;//????????????
            suggestion.setConveyTimes(conveyTimes);
            //??????????????????????????????????????????
            ConveySuggestionDto conveySuggestionDto = new ConveySuggestionDto();
            if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.MAIN_UNIT.getValue())) {
                suggestion.setUnit(unitRepository.findByUid(adjustConveyDto.getUnit()));
                conveySuggestionDto.setMainUnit(adjustConveyDto.getUnit());
            } else if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.CO_UNIT.getValue())) {
                List<String> spinsordId = Lists.newArrayList();
                spinsordId.add(adjustConveyDto.getUnit());
                conveySuggestionDto.setCoUnits(spinsordId);
            }
            //???????????????
            suggestion.setGovernmentUser(governmentUserRepository.findByAccountUid(userDetails.getUid()));
            suggestionRepository.saveAndFlush(suggestion);
            this.govConvey(suggestion, conveySuggestionDto, userDetails.getUid(), conveyTimes);
        }
        return body;
    }

    @Override
    public RespBody applyConvey(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = new RespBody();
        int begin = govSuggestionPageDto.getPage() - 1;
        Sort.Order viewSort = new Sort.Order(Sort.Direction.ASC, "govView");//????????????????????????
        Sort.Order createAt = new Sort.Order(Sort.Direction.DESC, "createTime");//????????????????????????
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(viewSort);
        orders.add(createAt);
        Sort sort = Sort.by(orders);
        Pageable page = PageRequest.of(begin, govSuggestionPageDto.getSize(), sort);
        Page<ConveyProcess> conveyProcessPage = this.getConveyProcess(userDetails, govSuggestionPageDto, page);
        PageVo<ConveyListVo> vo = new PageVo<>(conveyProcessPage, govSuggestionPageDto);
        List<ConveyListVo> conveyListVos = conveyProcessPage.getContent().stream().map(ConveyListVo::convertSug).collect(Collectors.toList());
        vo.setContent(conveyListVos);
        body.setData(vo);
        return body;
    }

    private Page<ConveyProcess> getConveyProcess(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, Pageable page){
        Page<ConveyProcess> conveyProcessPage = conveyProcessRepository.findAll((Specification<ConveyProcess>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("dealDone").as(Boolean.class)));//????????????????????????
            predicates.add(cb.equal(root.get("dealStatus").as(Byte.class),GovDealStatusEnum.NOT_DEAL.getValue()));//???????????????
            predicates.add(cb.equal(root.get("status").as(Byte.class),ConveyStatusEnum.CONVEY_FAILED.getValue()));//???????????????
            predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), govSuggestionPageDto.getLevel()));//?????????????????????????????????????????????
            predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (govSuggestionPageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return conveyProcessPage;
    }

    @Override
    public RespBody applyDelay(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = new RespBody();
        int begin = govSuggestionPageDto.getPage() - 1;
        Sort.Order viewSort = new Sort.Order(Sort.Direction.ASC, "govView");//????????????????????????
        Sort.Order createAt = new Sort.Order(Sort.Direction.DESC, "createTime");//????????????????????????
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(viewSort);
        orders.add(createAt);
        Sort sort = Sort.by(orders);
        Pageable page = PageRequest.of(begin, govSuggestionPageDto.getSize(), sort);
        Page<DelaySuggestion> delaySuggestionPage = this.getDelaySuggestion(userDetails, govSuggestionPageDto, page);
        PageVo<DelayListVo> vo = new PageVo<>(delaySuggestionPage, govSuggestionPageDto);
        List<DelayListVo> delaySuggestionVos = delaySuggestionPage.getContent().stream().map(DelayListVo::convert).collect(Collectors.toList());
        vo.setContent(delaySuggestionVos);
        body.setData(vo);
        return body;
    }

    private Page<DelaySuggestion> getDelaySuggestion(MobileUserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, Pageable page){
        Page<DelaySuggestion> delaySuggestionPage = delaySuggestionRepository.findAll((Specification<DelaySuggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("accept").as(Boolean.class)));//????????????????????????
            predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), govSuggestionPageDto.getLevel()));//?????????????????????????????????????????????
            predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (govSuggestionPageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return delaySuggestionPage;
    }

    @Override
    public RespBody urgeSug(MobileUserDetailsImpl userDetails, LevelDto levelDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(levelDto.getUid())) {
            String message = "??????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(levelDto.getUid());
        if (suggestion ==null){
            String message = "??????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        List<Urge> urges = urgeRepository.findBySuggestionUidAndType(levelDto.getUid(),UrgeScoreEnum.GOVERNMENT.getType());
        SuggestionSetting suggestionSetting = null;
        if (levelDto.getLevel().equals(LevelEnum.AREA.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(),userDetails.getArea().getUid());
        }else if (levelDto.getLevel().equals(LevelEnum.TOWN.getValue())){
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid());
        }
        if (suggestionSetting == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????????????????");
            return body;
        }
        Integer urgeFre = suggestionSetting.getUrgeFre();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -urgeFre);
        Date lastUrge = calendar.getTime();
        for (Urge urge : urges) {
            if (urge.getCreateTime().after(lastUrge)){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("???????????????????????? " +urgeFre+"?????????????????????????????????");
                return body;
            }
        }
        suggestion.setUrge(true);//??????????????????????????????
        suggestion.setUrgeLevel(suggestion.getUrgeLevel()+UrgeScoreEnum.GOVERNMENT.getScore());//??????????????????
        suggestionRepository.saveAndFlush(suggestion);
        Urge urge = new Urge();
        Account account = accountRepository.findByUid(userDetails.getUid());
        urge.setType(UrgeScoreEnum.GOVERNMENT.getType());//????????????
        urge.setScore(UrgeScoreEnum.GOVERNMENT.getScore());//????????????
        urge.setAccount(account);//????????????
        urge.setSuggestion(suggestion);//???????????????
        urgeRepository.saveAndFlush(urge);
        return body;
    }


    @Override
    public RespBody getSuggestionDetail(BaseDto baseDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(baseDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????");
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(baseDto.getUid());
        if (suggestion ==null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????");
            return body;
        }
        suggestion.setGovView(true);
        suggestionRepository.saveAndFlush(suggestion);
        GovSugDetailVo govSugDetailVo = GovSugDetailVo.convert(suggestion);
        body.setData(govSugDetailVo);
        return body;
    }

    @Override
    public RespBody getDelaySugDetail(BaseDto baseDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(baseDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????");
            return body;
        }
        DelaySuggestion delaySuggestion = delaySuggestionRepository.findByUid(baseDto.getUid());
        if (delaySuggestion ==null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????");
            return body;
        }
        delaySuggestion.setGovView(true);
        delaySuggestionRepository.saveAndFlush(delaySuggestion);
        DelaySuggestionVo delaySuggestionVo = DelaySuggestionVo.convert(delaySuggestion);
        body.setData(delaySuggestionVo);
        return body;
    }

    @Override
    public RespBody getAdjustSugDetail(BaseDto baseDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(baseDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????");
            return body;
        }
        ConveyProcess conveyProcess = conveyProcessRepository.findByUid(baseDto.getUid());
        if (conveyProcess ==null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????");
            return body;
        }
        conveyProcess.setGovView(true);
        conveyProcessRepository.saveAndFlush(conveyProcess);
        AdjustSuggestionVo adjustSuggestionVo = AdjustSuggestionVo.convert(conveyProcess);
        body.setData(adjustSuggestionVo);
        return body;
    }
}
