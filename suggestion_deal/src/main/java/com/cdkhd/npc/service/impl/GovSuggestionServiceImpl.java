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
        //查询代表的建议之前首先查询系统配置
        int begin = govSuggestionPageDto.getPage() - 1;
        generalService.scanSuggestions(userDetails);
        Sort.Order urge = new Sort.Order(Sort.Direction.DESC, "urge");//催办
        Sort.Order urgeLevel = new Sort.Order(Sort.Direction.DESC, "urgeLevel");//催办
        Sort.Order exceedLimit = new Sort.Order(Sort.Direction.DESC, "exceedLimit");//超期
        Sort.Order closeDeadLine = new Sort.Order(Sort.Direction.DESC, "closeDeadLine");//临期
        Sort.Order viewSort = new Sort.Order(Sort.Direction.ASC, "govView");//先按查看状态排序
        Sort.Order statusSort = new Sort.Order(Sort.Direction.ASC, "status");//先按状态排序
        Sort.Order createAt = new Sort.Order(Sort.Direction.DESC, "createTime");//再按创建时间排序
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

        String fileName = ExcelCode.encodeFileName("代表建议信息.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //暴露Content-Disposition响应头，以便前端可以获取文件名
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        String[] tableHeaders = new String[]{"编号", "建议类型", "建议标题", "提出时间", "提出代表", "建议内容", "所属地区", "联系方式", "审核人", "建议状态", "审核意见","审核日期","建议地点"};
        Sheet sheet = hssWb.createSheet("代表建议");
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

            // 编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // 建议类型
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(suggestion.getSuggestionBusiness().getName());

            // 建议标题
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(suggestion.getTitle());

            // 建议时间
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(simpleDateFormat.format(suggestion.getCreateTime()));

            // 建议代表
            Cell cell4 = row.createCell(4);
            NpcMember member = suggestion.getRaiser();
            if (member != null) {
                // 联系方式
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

            //建议内容
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(suggestion.getContent());

            //所属地区
            Cell cell6 = row.createCell(6);
            String place = "";
            if (suggestion.getLevel().equals(LevelEnum.AREA.getValue())) {
                place = suggestion.getArea().getName() + suggestion.getTown().getName();
            } else if (suggestion.getLevel().equals(LevelEnum.TOWN.getValue())) {
                place = suggestion.getTown().getName();  // + suggestion.getNpcMemberGroup().getName();
            }
            cell6.setCellValue(place);

            //审核人姓名
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(suggestion.getAuditor()!= null ? suggestion.getAuditor().getName():"");

            //状态
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(SuggestionStatusEnum.getName(suggestion.getStatus()));

            //审核意见
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(suggestion.getReason());

            //审核日期
            Cell cell11 = row.createCell(11);
            cell11.setCellValue(simpleDateFormat.format(suggestion.getAuditTime()));

            //建议级别
            Cell cell12 = row.createCell(12);
            cell12.setCellValue(LevelEnum.getName(suggestion.getLevel()));

        }
        try {
            hssWb.write(os);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("导出代表建议出错 \n {}", e1);
        }
    }

    private Page<Suggestion> getSuggestionPage(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto, Pageable page) {
        Page<Suggestion> suggestionPage = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));//如果是镇上的，就只能查询镇上的
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //标题
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + govSuggestionPageDto.getTitle() + "%"));
            }
            //类型
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestionBusiness").get("uid").as(String.class), govSuggestionPageDto.getBusiness()));
            }
            //提出代表
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMember())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + govSuggestionPageDto.getMember() + "%"));
            }
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMobile())) {
                predicates.add(cb.like(root.get("raiser").get("mobile").as(String.class), "%" +govSuggestionPageDto.getMobile() + "%"));
            }
            //办理单位
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getUnit())){
                Join<Suggestion, UnitSuggestion> join = root.join("unitSuggestions", JoinType.LEFT);
                predicates.add(cb.equal(join.get("unit").get("uid").as(String.class), govSuggestionPageDto.getUnit()));
            }
            if (null != govSuggestionPageDto.getSearchType()) {
                if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.WAIT_DEAL_SUG.getValue())){//待转交的建议
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//状态为已转交政府
                    Join<Suggestion, ConveyProcess> join = root.join("conveyProcesses", JoinType.LEFT);
                    predicates.add(cb.isNull(join));//没有转办记录的
                    //审核时间 开始
                    if (govSuggestionPageDto.getDateStart() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("auditTime").as(Date.class), govSuggestionPageDto.getDateStart()));
                    }
                    if (govSuggestionPageDto.getDateEnd() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("auditTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
                    }
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.ADJUST_UNIT_SUG.getValue())) { //申请调整单位的建议
                    Join<Suggestion, ConveyProcess> join = root.join("conveyProcesses", JoinType.LEFT);
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//状态为已转交政府
                    predicates.add(cb.equal(join.get("status").as(Byte.class), ConveyStatusEnum.CONVEY_FAILED.getValue()));//办理单位没有接受,转办失败
                    predicates.add(cb.equal(join.get("dealStatus").as(Byte.class), GovDealStatusEnum.NOT_DEAL.getValue()));//政府未对这次转办做出处理
                    predicates.add(cb.isFalse(join.get("dealDone").as(Boolean.class)));//这次转办未处理完成
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.APPLY_DELAY_SUG.getValue())) { //申请延期的建议
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));//状态为已转交单位
                    Join<Suggestion, DelaySuggestion> join = root.join("delaySuggestions", JoinType.LEFT);
                    predicates.add(cb.isNull(join.get("accept")));//政府还未处理这个延期
                    predicates.add(cb.equal(join.get("delayTimes").as(Integer.class), root.get("delayTimes").as(Integer.class)));//申请延期的办理次数是当前办理次数
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.DEALING_SUG.getValue())) {//办理中的建议
                    Predicate or = cb.or(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.TRANSFERRED_UNIT.getValue()),cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));
                    predicates.add(or);//状态为办理中的建议
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.FINISH_SUG.getValue())) {//已办完的建议
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));//状态为办理完成的建议
                    //办理完成时间 开始
                    if (govSuggestionPageDto.getDateStart() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("finishTime").as(Date.class), govSuggestionPageDto.getDateStart()));
                    }
                    if (govSuggestionPageDto.getDateEnd() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("finishTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
                    }
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.ACCOMPLISHED_SUG.getValue())) {//已办结的建议
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));//状态办结的建议
                    //办理完成时间 开始
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
     * 转办建议
     * @param userDetails
     * @param conveySuggestionDto
     * @return
     */
    @Override
    public RespBody conveySuggestion(UserDetailsImpl userDetails, ConveySuggestionDto conveySuggestionDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(conveySuggestionDto.getMainUnit()) || StringUtils.isEmpty(conveySuggestionDto.getUid())) {
            String message = "转交失败，请重试！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(conveySuggestionDto.getUid());
        suggestion.setStatus(SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());
        suggestion.setConveyTime(new Date());
        Integer conveyTimes = suggestion.getConveyTimes()==null?1:suggestion.getConveyTimes() + 1;//转办次数
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
        //政府处理人
        suggestion.setGovernmentUser(governmentUserRepository.findByAccountUid(userDetails.getUid()));
        suggestionRepository.saveAndFlush(suggestion);
        //政府方面转办流程记录办理单位
        this.govConvey(suggestion, conveySuggestionDto, userDetails.getUid(), conveyTimes);
        return body;
    }

    /**
     * 延期建议
     * @param userDetails
     * @param delaySuggestionDto
     * @return
     */
    @Override
    public RespBody delaySuggestion(UserDetailsImpl userDetails, DelaySuggestionDto delaySuggestionDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(delaySuggestionDto.getUid()) || delaySuggestionDto.getDelayDate() == null || delaySuggestionDto.getResult() == null) {
            String message = "延期失败，请重试！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        DelaySuggestion delaySuggestion = delaySuggestionRepository.findByUid(delaySuggestionDto.getUid());
        if (delaySuggestion == null) {
            String message = "找不到该建议的延期申请，请重试！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        delaySuggestion.setAccept(delaySuggestionDto.getResult());
        delaySuggestion.setRemark(delaySuggestionDto.getRemark());//审批原因
        if (delaySuggestionDto.getResult()) {
            delaySuggestion.setDelayTime(delaySuggestionDto.getDelayDate());//实际延期时间
            Suggestion suggestion = delaySuggestion.getSuggestion();//对应的建议
            UnitSuggestion unitSuggestion = delaySuggestion.getUnitSuggestion();//单位建议
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
            String message = "调整办理单位失败，请重试！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.MAIN_UNIT.getValue()) && StringUtils.isEmpty(adjustConveyDto.getUnit())) {
            String message = "主办单位必须选择,请重试！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        //先保存政府处理结果
        ConveyProcess conveyProcess = conveyProcessRepository.findByUid(adjustConveyDto.getUid());
        Suggestion suggestion = conveyProcess.getSuggestion();
        for (ConveyProcess process : suggestion.getConveyProcesses()) {
            if (!process.getUid().equals(adjustConveyDto.getUid()) && process.getUnit().getUid().equals(adjustConveyDto.getUnit()) && !process.getDealDone() && !process.getStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue())){
                String message = "该单位已经参与本条建议,请重新选择一个单位！";
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
        //然后保存新的办理单位信息
        if (adjustConveyDto.getDealStatus().equals(GovDealStatusEnum.RE_CONVEY.getValue()) && StringUtils.isNotEmpty(adjustConveyDto.getUnit())) {//如果有信息的办理单位，那么就保存，如果没有，那么就不处理
            Boolean acceptAll = true;//这个地方判断下，有可能不止一个单位申请调整，全部调整完了才能修改建议的状态
            Boolean isDealing = true;//这个建议未申请调整的其他建议是否已经开始办理，全部都开始办理的话，得把这个建议改为办理中
            for (ConveyProcess process : suggestion.getConveyProcesses()) {
                if (!process.getDealDone() && process.getStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue())){//有拒绝的建议，并且没有处理完这个拒绝，政府就还需要继续处理
                    //所有没有处理完的建议都没有被拒绝，就表示政府这边不需要在处理了
                    acceptAll = false;
                }
                //无需重新分配建议的时候，判断其他建议是否已经开办了
                if (!process.getDealDone()){
                    isDealing = false;
                }
            }
            if (acceptAll) {
                if (adjustConveyDto.getDealStatus().equals(ConveyStatusEnum.CONVEY_FAILED.getValue()) && isDealing){
                    suggestion.setStatus(SuggestionStatusEnum.HANDLING.getValue());//所有的建议开始办理了，需要把建议状态改为办理中
                }else{
                    suggestion.setStatus(SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());//还有些建议没有办理，那么就将建议改为待接收
                }
            }
            suggestion.setConveyTime(new Date());
            Integer conveyTimes = suggestion.getConveyTimes() + 1;//转办次数
            suggestion.setConveyTimes(conveyTimes);
            ConveySuggestionDto conveySuggestionDto = new ConveySuggestionDto();
            if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.MAIN_UNIT.getValue())) {
                suggestion.setUnit(unitRepository.findByUid(adjustConveyDto.getUid()));
                conveySuggestionDto.setMainUnit(adjustConveyDto.getUnit());
            } else if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.CO_UNIT.getValue())) {
                List<String> spinsordId = Lists.newArrayList();
                spinsordId.add(adjustConveyDto.getUnit());
                conveySuggestionDto.setCoUnits(spinsordId);
            }
            //政府处理人
            suggestion.setGovernmentUser(governmentUserRepository.findByAccountUid(userDetails.getUid()));
            suggestionRepository.saveAndFlush(suggestion);
            this.govConvey(suggestion, conveySuggestionDto, userDetails.getUid(), conveyTimes);
        }
        return body;
    }

    /**
     * 政府方面转办流程记录办理单位
     *
     * @param suggestion
     * @param conveySuggestionDto
     * @param uid
     * @param conveyTimes
     */
    private void govConvey(Suggestion suggestion, ConveySuggestionDto conveySuggestionDto, String uid, Integer conveyTimes) {
        Set<ConveyProcess> conveyProcessList = Sets.newHashSet();
        //主办单位
        if (conveySuggestionDto.getMainUnit() != null) {
            ConveyProcess conveyProcess = new ConveyProcess();
            conveyProcess.setGovernmentUser(governmentUserRepository.findByAccountUid(uid));
            conveyProcess.setUnit(unitRepository.findByUid(conveySuggestionDto.getMainUnit()));
            conveyProcess.setType(UnitTypeEnum.MAIN_UNIT.getValue());
            conveyProcess.setConveyTimes(conveyTimes);
            conveyProcess.setSuggestion(suggestion);
            conveyProcessList.add(conveyProcess);
        }
        //协办单位
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
            predicates.add(cb.isFalse(root.get("dealDone").as(Boolean.class)));//这个转办没有完成
            predicates.add(cb.equal(root.get("dealStatus").as(Byte.class),GovDealStatusEnum.NOT_DEAL.getValue()));//政府未处理
            predicates.add(cb.equal(root.get("status").as(Byte.class),ConveyStatusEnum.CONVEY_FAILED.getValue()));//单位拒绝了
            predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), userDetails.getLevel()));//如果是镇上的，就只能查询镇上的
            predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //标题
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getTitle())) {
                predicates.add(cb.like(root.get("suggestion").get("title").as(String.class), "%" + govSuggestionPageDto.getTitle() + "%"));
            }
            //类型
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestion").get("suggestionBusiness").get("uid").as(String.class), govSuggestionPageDto.getBusiness()));
            }
            //提出代表
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMember())) {
                predicates.add(cb.like(root.get("suggestion").get("raiser").get("name").as(String.class), "%" + govSuggestionPageDto.getMember() + "%"));
            }
            //单位处理时间 开始
            if (govSuggestionPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("unitDealTime").as(Date.class), govSuggestionPageDto.getDateStart()));
            }
            if (govSuggestionPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("unitDealTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
            }
            //办理单位
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
            predicates.add(cb.isNull(root.get("accept").as(Boolean.class)));//这个转办没有完成
            predicates.add(cb.equal(root.get("suggestion").get("level").as(Byte.class), userDetails.getLevel()));//如果是镇上的，就只能查询镇上的
            predicates.add(cb.equal(root.get("suggestion").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("suggestion").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //标题
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getTitle())) {
                predicates.add(cb.like(root.get("suggestion").get("title").as(String.class), "%" + govSuggestionPageDto.getTitle() + "%"));
            }
            //类型
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestion").get("suggestionBusiness").get("uid").as(String.class), govSuggestionPageDto.getBusiness()));
            }
            //提出代表
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getMember())) {
                predicates.add(cb.like(root.get("suggestion").get("raiser").get("name").as(String.class), "%" + govSuggestionPageDto.getMember() + "%"));
            }
            //单位处理时间 开始
            if (govSuggestionPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), govSuggestionPageDto.getDateStart()));
            }
            if (govSuggestionPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
            }
            //办理单位
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
            String message = "找不到建议！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(baseDto.getUid());
        if (suggestion ==null){
            String message = "找不到建议！";
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
            body.setMessage("系统错误，请联系管理员！");
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
                body.setMessage("该建议已经催办！ " +urgeFre+"天之内，不能再次催办！");
                return body;
            }
        }
        suggestion.setUrge(true);//将建议的改为催办状态
        suggestion.setUrgeLevel(suggestion.getUrgeLevel()+UrgeScoreEnum.GOVERNMENT.getScore());//增加催办分数
        suggestionRepository.saveAndFlush(suggestion);
        Urge urge = new Urge();
        Account account = accountRepository.findByUid(userDetails.getUid());
        urge.setType(UrgeScoreEnum.GOVERNMENT.getType());//政府催办
        urge.setScore(UrgeScoreEnum.GOVERNMENT.getScore());//催办分数
        urge.setAccount(account);//催办账号
        urge.setSuggestion(suggestion);//催办的建议
        urgeRepository.saveAndFlush(urge);
        return body;
    }

}
