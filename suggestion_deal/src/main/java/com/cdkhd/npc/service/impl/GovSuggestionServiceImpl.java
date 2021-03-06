package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.AdjustConveyDto;
import com.cdkhd.npc.entity.dto.ConveySuggestionDto;
import com.cdkhd.npc.entity.dto.DelaySuggestionDto;
import com.cdkhd.npc.entity.dto.GovSuggestionPageDto;
import com.cdkhd.npc.entity.vo.ConveyProcessVo;
import com.cdkhd.npc.entity.vo.DelaySuggestionVo;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.base.DelaySuggestionRepository;
import com.cdkhd.npc.repository.base.GovernmentUserRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.SuggestionSettingRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitRepository;
import com.cdkhd.npc.repository.suggestion_deal.UrgeRepository;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.GovSuggestionService;
import com.cdkhd.npc.util.ExcelCode;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    private GeneralService generalService;


    @Autowired
    public GovSuggestionServiceImpl(SuggestionRepository suggestionRepository, UnitRepository unitRepository, GovernmentUserRepository governmentUserRepository, ConveyProcessRepository conveyProcessRepository, DelaySuggestionRepository delaySuggestionRepository, UrgeRepository urgeRepository, SuggestionSettingRepository suggestionSettingRepository, AccountRepository accountRepository, GeneralService generalService) {
        this.suggestionRepository = suggestionRepository;
        this.unitRepository = unitRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.conveyProcessRepository = conveyProcessRepository;
        this.delaySuggestionRepository = delaySuggestionRepository;
        this.urgeRepository = urgeRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
        this.accountRepository = accountRepository;
        this.generalService = generalService;
    }

    @Override
    public RespBody getGovSuggestion(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = new RespBody();
        //???????????????????????????????????????????????????
        int begin = govSuggestionPageDto.getPage() - 1;
        generalService.scanSuggestions(userDetails);
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
        PageVo<SuggestionVo> vo = new PageVo<>(suggestionPage, govSuggestionPageDto);
        List<SuggestionVo> suggestionVos = suggestionPage.getContent().stream().map(SuggestionVo::convert).collect(Collectors.toList());
        vo.setContent(suggestionVos);
        body.setData(vo);
        return body;
    }

    @Override
    public void exportGovSuggestion(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, HttpServletRequest req, HttpServletResponse res) {
        ServletOutputStream os = null;
        try {
            os = res.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Workbook hssWb = new HSSFWorkbook();
        int begin = govSuggestionPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, govSuggestionPageDto.getSize(), Sort.Direction.fromString(govSuggestionPageDto.getDirection()), govSuggestionPageDto.getProperty());
        List<Suggestion> suggestions = this.getSuggestionPage(userDetails, govSuggestionPageDto, page).getContent();

        String fileName = ExcelCode.encodeFileName("??????????????????.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //??????Content-Disposition?????????????????????????????????????????????
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        String[] tableHeaders = new String[]{"??????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "?????????", "????????????", "????????????","????????????","????????????"};
        Sheet sheet = hssWb.createSheet("????????????");
        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Suggestion suggestion : suggestions) {
            Row row = sheet.createRow(beginIndex);

            // ??????
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // ????????????
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(suggestion.getSuggestionBusiness().getName());

            // ????????????
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(suggestion.getTitle());

            // ????????????
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(simpleDateFormat.format(suggestion.getCreateTime()));

            // ????????????
            Cell cell4 = row.createCell(4);
            NpcMember member = suggestion.getRaiser();
            if (member != null) {
                // ????????????
                Cell cell7 = row.createCell(7);
                cell4.setCellValue(member.getName());
                String mobile = member.getMobile();
                if (StringUtils.isNotBlank(mobile)) {
                    cell7.setCellValue(mobile);
                } else {
                    cell7.setCellValue("");
                }
            } else {
                cell2.setCellValue("");
            }

            //????????????
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(suggestion.getContent());

            //????????????
            Cell cell6 = row.createCell(6);
            String place = "";
            if (suggestion.getLevel().equals(LevelEnum.AREA.getValue())) {
                place = suggestion.getArea().getName() + suggestion.getTown().getName();
            } else if (suggestion.getLevel().equals(LevelEnum.TOWN.getValue())) {
                place = suggestion.getTown().getName();  // + suggestion.getNpcMemberGroup().getName();
            }
            cell6.setCellValue(place);

            //???????????????
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(suggestion.getAuditor()!= null ? suggestion.getAuditor().getName():"");

            //??????
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(SuggestionStatusEnum.getName(suggestion.getStatus()));

            //????????????
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(suggestion.getReason());

            //????????????
            Cell cell11 = row.createCell(11);
            cell11.setCellValue(simpleDateFormat.format(suggestion.getAuditTime()));

            //????????????
            Cell cell12 = row.createCell(12);
            cell12.setCellValue(LevelEnum.getName(suggestion.getLevel()));

        }
        try {
            hssWb.write(os);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("???????????????????????? \n {}", e1);
        }
    }

    private Page<Suggestion> getSuggestionPage(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, Pageable page) {
        Page<Suggestion> suggestionPage = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));//?????????????????????????????????????????????
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //??????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + govSuggestionPageDto.getTitle() + "%"));
            }
            //??????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestionBusiness").get("uid").as(String.class), govSuggestionPageDto.getBusiness()));
            }
            //????????????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMember())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + govSuggestionPageDto.getMember() + "%"));
            }
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMobile())) {
                predicates.add(cb.like(root.get("raiser").get("mobile").as(String.class), "%" +govSuggestionPageDto.getMobile() + "%"));
            }
            //????????????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getUnit())){
                Join<Suggestion, UnitSuggestion> join = root.join("unitSuggestions", JoinType.LEFT);
                predicates.add(cb.equal(join.get("unit").get("uid").as(String.class), govSuggestionPageDto.getUnit()));
            }
            if (null != govSuggestionPageDto.getSearchType()) {
                if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.WAIT_DEAL_SUG.getValue())){//??????????????????
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//????????????????????????
                    Join<Suggestion, ConveyProcess> join = root.join("conveyProcesses", JoinType.LEFT);
                    predicates.add(cb.isNull(join));//?????????????????????
                    //???????????? ??????
                    if (govSuggestionPageDto.getDateStart() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("auditTime").as(Date.class), govSuggestionPageDto.getDateStart()));
                    }
                    if (govSuggestionPageDto.getDateEnd() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("auditTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
                    }
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
                    //?????????????????? ??????
                    if (govSuggestionPageDto.getDateStart() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("finishTime").as(Date.class), govSuggestionPageDto.getDateStart()));
                    }
                    if (govSuggestionPageDto.getDateEnd() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("finishTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
                    }
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.ACCOMPLISHED_SUG.getValue())) {//??????????????????
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));//?????????????????????
                    //?????????????????? ??????
                    if (govSuggestionPageDto.getDateStart() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("accomplishTime").as(Date.class), govSuggestionPageDto.getDateStart()));
                    }
                    if (govSuggestionPageDto.getDateEnd() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("accomplishTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
                    }
                }
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return suggestionPage;
    }


    /**
     * ????????????
     * @param userDetails
     * @param conveySuggestionDto
     * @return
     */
    @Override
    public RespBody conveySuggestion(UserDetailsImpl userDetails, ConveySuggestionDto conveySuggestionDto) {
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
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
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
     * ????????????
     * @param userDetails
     * @param delaySuggestionDto
     * @return
     */
    @Override
    public RespBody delaySuggestion(UserDetailsImpl userDetails, DelaySuggestionDto delaySuggestionDto) {
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
        if (delaySuggestionDto.getResult()) {
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
    public RespBody adjustConvey(UserDetailsImpl userDetails, AdjustConveyDto adjustConveyDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(adjustConveyDto.getUid()) || adjustConveyDto.getUnitType() == null) {
            String message = "???????????????????????????????????????";
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
                if (!process.getDealDone()){
                    isDealing = false;
                }
            }
            if (acceptAll) {
                if (adjustConveyDto.getDealStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue()) && isDealing){
                    suggestion.setStatus(SuggestionStatusEnum.HANDLING.getValue());//?????????????????????????????????????????????????????????????????????
                }else{
                    suggestion.setStatus(SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());//???????????????????????????????????????????????????????????????
                }
            }
            suggestion.setConveyTime(new Date());
            Integer conveyTimes = suggestion.getConveyTimes() + 1;//????????????
            suggestion.setConveyTimes(conveyTimes);
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
            this.govConvey(suggestion, conveySuggestionDto, userDetails.getUid(), conveyTimes);
            suggestionRepository.saveAndFlush(suggestion);
        }
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
    public RespBody applyConvey(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = new RespBody();
        int begin = govSuggestionPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, govSuggestionPageDto.getSize(), Sort.Direction.fromString(govSuggestionPageDto.getDirection()), govSuggestionPageDto.getProperty());
        Page<ConveyProcess> conveyProcessPage = this.getConveyProcess(userDetails, govSuggestionPageDto, page);
        PageVo<ConveyProcessVo> vo = new PageVo<>(conveyProcessPage, govSuggestionPageDto);
        List<ConveyProcessVo> conveyProcessVos = conveyProcessPage.getContent().stream().map(ConveyProcessVo::convertSug).collect(Collectors.toList());
        vo.setContent(conveyProcessVos);
        body.setData(vo);
        return body;
    }

    private Page<ConveyProcess> getConveyProcess(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, Pageable page){
        Page<ConveyProcess> conveyProcessPage = conveyProcessRepository.findAll((Specification<ConveyProcess>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("dealDone").as(Boolean.class)));//????????????????????????
            predicates.add(cb.equal(root.get("dealStatus").as(Byte.class),GovDealStatusEnum.NOT_DEAL.getValue()));//???????????????
            predicates.add(cb.equal(root.get("status").as(Byte.class),ConveyStatusEnum.CONVEY_FAILED.getValue()));//???????????????
            predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), userDetails.getLevel()));//?????????????????????????????????????????????
            predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //??????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getTitle())) {
                predicates.add(cb.like(root.get("suggestion").get("title").as(String.class), "%" + govSuggestionPageDto.getTitle() + "%"));
            }
            //??????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestion").get("suggestionBusiness").get("uid").as(String.class), govSuggestionPageDto.getBusiness()));
            }
            //????????????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMember())) {
                predicates.add(cb.like(root.get("suggestion").get("raiser").get("name").as(String.class), "%" + govSuggestionPageDto.getMember() + "%"));
            }
            //?????????????????? ??????
            if (govSuggestionPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("unitDealTime").as(Date.class), govSuggestionPageDto.getDateStart()));
            }
            if (govSuggestionPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("unitDealTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
            }
            //????????????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getUnit())){
                predicates.add(cb.equal(root.get("unit").get("uid").as(String.class), govSuggestionPageDto.getUnit()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return conveyProcessPage;
    }


    @Override
    public RespBody applyDelay(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = new RespBody();
        int begin = govSuggestionPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, govSuggestionPageDto.getSize(), Sort.Direction.fromString(govSuggestionPageDto.getDirection()), govSuggestionPageDto.getProperty());
        Page<DelaySuggestion> delaySuggestionPage = this.getDelaySuggestion(userDetails, govSuggestionPageDto, page);
        PageVo<DelaySuggestionVo> vo = new PageVo<>(delaySuggestionPage, govSuggestionPageDto);
        List<DelaySuggestionVo> delaySuggestionVos = delaySuggestionPage.getContent().stream().map(DelaySuggestionVo::convert).collect(Collectors.toList());
        vo.setContent(delaySuggestionVos);
        body.setData(vo);
        return body;
    }

    private Page<DelaySuggestion> getDelaySuggestion(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, Pageable page){
        Page<DelaySuggestion> delaySuggestionPage = delaySuggestionRepository.findAll((Specification<DelaySuggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("accept").as(Boolean.class)));//????????????????????????
            predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), userDetails.getLevel()));//?????????????????????????????????????????????
            predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //??????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getTitle())) {
                predicates.add(cb.like(root.get("suggestion").get("title").as(String.class), "%" + govSuggestionPageDto.getTitle() + "%"));
            }
            //??????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestion").get("suggestionBusiness").get("uid").as(String.class), govSuggestionPageDto.getBusiness()));
            }
            //????????????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMember())) {
                predicates.add(cb.like(root.get("suggestion").get("raiser").get("name").as(String.class), "%" + govSuggestionPageDto.getMember() + "%"));
            }
            //?????????????????? ??????
            if (govSuggestionPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), govSuggestionPageDto.getDateStart()));
            }
            if (govSuggestionPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
            }
            //????????????
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getUnit())){
                predicates.add(cb.equal(root.get("unitSuggestion").get("unit").get("uid").as(String.class), govSuggestionPageDto.getUnit()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return delaySuggestionPage;
    }


    @Override
    public RespBody urgeSug(UserDetailsImpl userDetails, BaseDto baseDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(baseDto.getUid())) {
            String message = "??????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(baseDto.getUid());
        if (suggestion ==null){
            String message = "??????????????????";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        List<Urge> urges = urgeRepository.findBySuggestionUidAndType(baseDto.getUid(),UrgeScoreEnum.GOVERNMENT.getType());
        SuggestionSetting suggestionSetting = null;
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(),userDetails.getArea().getUid());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
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

}
