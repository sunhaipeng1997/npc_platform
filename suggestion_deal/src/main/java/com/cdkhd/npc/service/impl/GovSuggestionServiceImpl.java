package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.AdjustConveyDto;
import com.cdkhd.npc.entity.dto.ConveySuggestionDto;
import com.cdkhd.npc.entity.dto.DelaySuggestionDto;
import com.cdkhd.npc.entity.dto.GovSuggestionPageDto;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.base.DelaySuggestionRepository;
import com.cdkhd.npc.repository.base.GovernmentUserRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitRepository;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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



    @Autowired
    public GovSuggestionServiceImpl(SuggestionRepository suggestionRepository, UnitRepository unitRepository, GovernmentUserRepository governmentUserRepository, ConveyProcessRepository conveyProcessRepository, DelaySuggestionRepository delaySuggestionRepository) {
        this.suggestionRepository = suggestionRepository;
        this.unitRepository = unitRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.conveyProcessRepository = conveyProcessRepository;
        this.delaySuggestionRepository = delaySuggestionRepository;
    }

    @Override
    public RespBody getGovSuggestion(UserDetailsImpl userDetails, GovSuggestionPageDto govSuggestionPageDto) {
        RespBody body = new RespBody();
        //查询代表的建议之前首先查询系统配置
        int begin = govSuggestionPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, govSuggestionPageDto.getSize(), Sort.Direction.fromString(govSuggestionPageDto.getDirection()), govSuggestionPageDto.getProperty());
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
                predicates.add(cb.equal(root.get("raiser").get("mobile").as(String.class), govSuggestionPageDto.getMobile()));
            }
            //审核时间 开始
            if (govSuggestionPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("auditTime").as(Date.class), govSuggestionPageDto.getDateStart()));
            }
            if (govSuggestionPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("auditTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
            }
            //办理单位
            if (StringUtils.isNotEmpty(govSuggestionPageDto.getUnit())){
                predicates.add(cb.equal(root.get("auditTime").as(Date.class), govSuggestionPageDto.getDateEnd()));
            }
            if (null != govSuggestionPageDto.getSearchType()) {
                if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.WAIT_DEAL_SUG.getValue())){//待转交的建议
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//状态为已转交政府
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.ADJUST_UNIT_SUG.getValue())) { //申请调整单位的建议
                    Join<Suggestion, ConveyProcess> join = root.join("ConveyProcesses", JoinType.LEFT);
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
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue()));//状态为办理中的建议
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.FINISH_SUG.getValue())) {//已办完的建议
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));//状态为办理完成的建议
                }else if (govSuggestionPageDto.getSearchType().equals(GovSugTypeEnum.ACCOMPLISHED_SUG.getValue())) {//已办结的建议
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));//状态办结的建议
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
        Integer conveyTimes = suggestion.getConveyTimes() + 1;//转办次数
        suggestion.setConveyTimes(conveyTimes);
        suggestion.setUnit(unitRepository.findByUid(conveySuggestionDto.getMainUnit()));
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
            String message = "申请延期失败，请重试！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(delaySuggestionDto.getUid());
        DelaySuggestion delaySuggestion = null;
        for (DelaySuggestion suggestionDelaySuggestion : suggestion.getDelaySuggestions()) {
            if (suggestionDelaySuggestion.getAccept() == null && suggestionDelaySuggestion.getDelayTimes().equals(suggestion.getDelayTimes())) {
                delaySuggestion = suggestionDelaySuggestion;
            }
        }
        if (delaySuggestion == null) {
            String message = "找不到该建议的延期申请，请重试！";
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(message);
            return body;
        }
        delaySuggestion.setAccept(delaySuggestionDto.getResult());
        delaySuggestion.setDelayTime(delaySuggestionDto.getDelayDate());//实际延期时间
        delaySuggestion.setRemark(delaySuggestionDto.getRemark());//审批原因
        if (delaySuggestionDto.getResult()){
            suggestion.setDelayTimes(suggestion.getDelayTimes()+1);
            suggestion.setExpectDate(delaySuggestionDto.getDelayDate());
//            delaySuggestion.setDelayTimes(suggestion.getDelayTimes()+1);
            delaySuggestion.setSuggestion(suggestion);
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
            if (process.getUnit().getUid().equals(adjustConveyDto.getUnit()) && !process.getDealDone()){
                String message = "该单位已经参与本条建议,请重新选择一个单位！";
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage(message);
                return body;
            }
        }
        conveyProcess.setStatus(ConveyStatusEnum.CONVEYING.getValue());
        conveyProcess.setDealStatus(adjustConveyDto.getDealStatus());
        conveyProcess.setDealDone(true);
        conveyProcessRepository.saveAndFlush(conveyProcess);
        //然后保存新的办理单位信息
        if (StringUtils.isNotEmpty(adjustConveyDto.getUnit())) {//如果有信息的办理单位，那么就保存，如果没有，那么就不处理
            suggestion.setStatus(SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());
            suggestion.setConveyTime(new Date());
            Integer conveyTimes = suggestion.getConveyTimes() + 1;//转办次数
            suggestion.setConveyTimes(conveyTimes);
            //政府方面转办流程记录办理单位
            ConveySuggestionDto conveySuggestionDto = new ConveySuggestionDto();
            if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.MAIN_UNIT.getValue())) {
                suggestion.setUnit(unitRepository.findByUid(adjustConveyDto.getUid()));
                conveySuggestionDto.setMainUnit(adjustConveyDto.getUid());
            } else if (adjustConveyDto.getUnitType().equals(UnitTypeEnum.CO_UNIT.getValue())) {
                List<String> spinsordId = Lists.newArrayList();
                spinsordId.add(adjustConveyDto.getUid());
                conveySuggestionDto.setCoUnits(spinsordId);
            }
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
            conveyProcess.setGovernmentUser(governmentUserRepository.findByUid(uid));
            conveyProcess.setUnit(unitRepository.findByUid(conveySuggestionDto.getMainUnit()));
            conveyProcess.setType(UnitTypeEnum.MAIN_UNIT.getValue());
            conveyProcess.setConveyTimes(conveyTimes);
            conveyProcess.setSuggestion(suggestion);
            conveyProcessList.add(conveyProcess);
        }
        //协办单位
        if (CollectionUtils.isNotEmpty(conveySuggestionDto.getCoUnits())) {
            for (String coUnit : conveySuggestionDto.getCoUnits()) {
                ConveyProcess coUnitCnvey = new ConveyProcess();
                coUnitCnvey.setGovernmentUser(governmentUserRepository.findByUid(uid));
                coUnitCnvey.setUnit(unitRepository.findByUid(conveySuggestionDto.getMainUnit()));
                coUnitCnvey.setConveyTimes(conveyTimes);
                coUnitCnvey.setType(UnitTypeEnum.CO_UNIT.getValue());
                coUnitCnvey.setSuggestion(suggestion);
                conveyProcessList.add(coUnitCnvey);
            }
        }
        if (CollectionUtils.isNotEmpty(conveyProcessList)) {
            conveyProcessRepository.saveAll(conveyProcessList);
        }
    }

}
