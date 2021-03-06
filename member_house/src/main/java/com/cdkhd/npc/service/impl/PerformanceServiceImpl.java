package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.MemberCountDto;
import com.cdkhd.npc.entity.dto.PerformanceDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeAddDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeDto;
import com.cdkhd.npc.entity.vo.CountVo;
import com.cdkhd.npc.entity.vo.MemberCountVo;
import com.cdkhd.npc.entity.vo.PerformanceTypeVo;
import com.cdkhd.npc.entity.vo.PerformanceVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.PerformanceStatusEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.util.Constant;
import com.cdkhd.npc.util.ExcelCode;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
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
public class PerformanceServiceImpl implements PerformanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    private SystemSettingRepository systemSettingRepository;

    private NpcMemberRepository npcMemberRepository;

    private TownRepository townRepository;

    @Autowired
    public PerformanceServiceImpl(PerformanceRepository performanceRepository, PerformanceTypeRepository performanceTypeRepository, SystemSettingRepository systemSettingRepository, NpcMemberRepository npcMemberRepository, TownRepository townRepository) {
        this.performanceRepository = performanceRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.townRepository = townRepository;
    }

    /**
     * ????????????????????????
     *
     * @param userDetails
     * @param performanceTypeDto
     * @return
     */
    @Override
    public RespBody findPerformanceType(UserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto) {
        RespBody body = new RespBody();
        int begin = performanceTypeDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performanceTypeDto.getSize(), Sort.Direction.fromString(performanceTypeDto.getDirection()), performanceTypeDto.getProperty());

        Page<PerformanceType> performanceTypePage = performanceTypeRepository.findAll((Specification<PerformanceType>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            }
            if (performanceTypeDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), performanceTypeDto.getStatus()));
            }
            //???????????????
            if (StringUtils.isNotEmpty(performanceTypeDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + performanceTypeDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);

        PageVo<PerformanceTypeVo> vo = new PageVo<>(performanceTypePage, performanceTypeDto);
        List<PerformanceTypeVo> performanceTypes = performanceTypePage.getContent().stream().map(PerformanceTypeVo::convert).collect(Collectors.toList());
        vo.setContent(performanceTypes);
        body.setData(vo);

        return body;
    }

    /**
     * ???????????????????????????
     *
     * @param userDetails
     * @param performanceTypeAddDto
     * @return
     */
    @Override
    public RespBody addOrUpdatePerformanceType(UserDetailsImpl userDetails, PerformanceTypeAddDto performanceTypeAddDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(performanceTypeAddDto.getName())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????");
            return body;
        }
        PerformanceType performanceType = null;
        if (StringUtils.isEmpty(performanceTypeAddDto.getUid())) {//??????????????????
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndTownUidAndIsDelFalse(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getTown().getUid());
            }if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndAreaUidAndIsDelFalse(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getArea().getUid());
            }
        }else{//??????????????????
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndTownUidAndIsDelFalseAndUidIsNot(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getTown().getUid(),performanceTypeAddDto.getUid());
            }if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndAreaUidAndIsDelFalseAndUidIsNot(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getArea().getUid(),performanceTypeAddDto.getUid());
            }
        }

        if (performanceType != null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????");
            return body;
        }
        //??????uid?????????????????????????????????
        if (StringUtils.isNotEmpty(performanceTypeAddDto.getUid())) {
            performanceType = performanceTypeRepository.findByUid(performanceTypeAddDto.getUid());
            if (performanceType == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("???????????????????????????");
                return body;
            }
        } else {
            //uid???????????????????????????
            performanceType = new PerformanceType();
            performanceType.setLevel(userDetails.getLevel());
            performanceType.setArea(userDetails.getArea());
            performanceType.setTown(userDetails.getTown());
            Integer maxSequence = 0;
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                maxSequence = performanceTypeRepository.findMaxSequenceByLevelAndTownUid(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid());
            }if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                maxSequence = performanceTypeRepository.findMaxSequenceByLevelAndAreaUid(LevelEnum.AREA.getValue(),userDetails.getArea().getUid());
            }
            if(maxSequence == null){//?????????????????????????????????????????????????????????????????????0??????????????????
                maxSequence = 0;
            }
            performanceType.setSequence(maxSequence + 1);
            performanceType.setIsDefault(false);
        }
        performanceType.setName(performanceTypeAddDto.getName());
        performanceType.setRemark(performanceTypeAddDto.getRemark());

        performanceTypeRepository.saveAndFlush(performanceType);
        return body;
    }

    /**
     * ??????????????????
     *
     * @param uid
     * @return
     */
    @Override
    public RespBody deletePerformanceType(String uid) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        if (performanceType.getIsDefault()) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????????????????");
            return body;
        }
        performanceType.setIsDel(true);
        performanceTypeRepository.saveAndFlush(performanceType);
        return body;
    }

    /**
     * ??????????????????
     *
     * @param uid  ??????uid
     * @param type ??????  ??????
     * @return
     */
    @Override
    public RespBody changeTypeSequence(UserDetailsImpl userDetails, String uid, Byte type) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        PerformanceType targetType = null;
        if (type.equals(Constant.LOGIN_WAY_UP)) {//1 ??????
            Sort sort = new Sort(Sort.Direction.DESC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                targetType = performanceTypeRepository.findBySequenceAndLevelTownUidDesc(performanceType.getSequence(),userDetails.getLevel(),userDetails.getTown().getUid(), page).getContent().get(0);
            }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                targetType = performanceTypeRepository.findBySequenceAndLevelAreaUidDesc(performanceType.getSequence(),userDetails.getLevel(),userDetails.getArea().getUid(), page).getContent().get(0);
            }
        } else {
            Sort sort = new Sort(Sort.Direction.ASC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                targetType = performanceTypeRepository.findBySequenceAndLevelTownUidAsc(performanceType.getSequence(),userDetails.getLevel(),userDetails.getTown().getUid(), page).getContent().get(0);
            }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                targetType = performanceTypeRepository.findBySequenceAndLevelAreaUidAsc(performanceType.getSequence(),userDetails.getLevel(),userDetails.getArea().getUid(), page).getContent().get(0);
            }
        }
        if (targetType == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????");
            return body;
        }
        List<PerformanceType> types = this.changeSequence(performanceType, targetType);
        performanceTypeRepository.saveAll(types);
        return body;
    }

    /**
     * ????????????????????????
     *
     * @param uid
     * @param status 1 ?????? 2 ??????
     * @return
     */
    @Override
    public RespBody changeTypeStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        performanceType.setStatus(status);
        performanceTypeRepository.saveAndFlush(performanceType);
        return body;
    }

    @Override
    public RespBody performanceTypeList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<PerformanceType> performanceTypes = Lists.newArrayList();
        //??????????????????????????????????????????
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))){
            performanceTypes = performanceTypeRepository.findByLevelAndAreaUidAndIsDelFalse(userDetails.getLevel(),userDetails.getArea().getUid());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypes = performanceTypeRepository.findByLevelAndTownUidAndIsDelFalse(userDetails.getLevel(),userDetails.getTown().getUid());
        }
        List<CommonVo> commonVos = performanceTypes.stream().map(type -> CommonVo.convert(type.getUid(),type.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }


    @Override
    public RespBody subTownPerformanceTypeList(String townUid) {
        RespBody body = new RespBody();
        List<PerformanceType> performanceTypes = Lists.newArrayList();
        Town town = townRepository.findByUid(townUid);
        if (town != null && town.getType().equals(LevelEnum.AREA.getValue())){//???????????????????????????????????????????????????
            performanceTypes = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), town.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (StringUtils.isNotEmpty(townUid)){//?????????
            performanceTypes = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(), townUid, StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = performanceTypes.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody findPerformance(UserDetailsImpl userDetails, PerformanceDto performanceDto) {
        RespBody body = new RespBody();
        //???????????????????????????????????????????????????
        int begin = performanceDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performanceDto.getSize(), Sort.Direction.fromString(performanceDto.getDirection()), performanceDto.getProperty());
        Page<Performance> performancePage = this.getPerformancePage(userDetails,performanceDto, page);
        PageVo<PerformanceVo> vo = new PageVo<>(performancePage, performanceDto);
        List<PerformanceVo> performances = performancePage.getContent().stream().map(PerformanceVo::convert).collect(Collectors.toList());
        vo.setContent(performances);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody deletePerformance(String uid) {
        RespBody body = new RespBody();
        Performance performance = performanceRepository.findByUid(uid);
        if (performance == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        performance.setIsDel(true);
        performanceRepository.saveAndFlush(performance);
        return body;
    }

    private Page<Performance> getPerformancePage(UserDetailsImpl userDetails, PerformanceDto performanceDto, Pageable page) {
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_SUCCESS.getValue()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                SystemSetting systemSetting = this.getSystemSetting(userDetails);
                if (systemSetting.getShowSubPerformance() && performanceDto.isFlag()) {//????????????????????????
                    List<NpcMember> areaMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
                    List<NpcMember> allMembers = Lists.newArrayList();
                    for (NpcMember areaMember : areaMembers) {
                        if (areaMember.getAccount()!=null) {//??????????????????
                            allMembers.addAll(areaMember.getAccount().getNpcMembers());
                        }else{//??????????????????
                            allMembers.add(areaMember);
                        }
                    }
                    List<String> memberUid = Lists.newArrayList();
                    for (NpcMember member : allMembers) {
                        memberUid.add(member.getUid());
                    }
                    if (CollectionUtils.isNotEmpty(memberUid)) {
                        predicates.add(cb.in(root.get("npcMember").get("uid")).value(memberUid));
                    }
                }else{//?????????????????????????????????
                    //?????????
                    if (!performanceDto.isFlag()) {
                        predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                        if ( StringUtils.isNotEmpty(performanceDto.getTownUid()) ){
                            predicates.add(cb.equal(root.get("town").get("uid").as(String.class), performanceDto.getTownUid()));
                        }
                    }else {//?????????
                        predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
                    }
                }
            }
            //??????
            if (StringUtils.isNotEmpty(performanceDto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + performanceDto.getTitle() + "%"));
            }

            //??????
            if (StringUtils.isNotEmpty(performanceDto.getPerformanceType())) {
                predicates.add(cb.equal(root.get("performanceType").get("uid").as(String.class), performanceDto.getPerformanceType()));
            }
            //????????????
            if (StringUtils.isNotEmpty(performanceDto.getName())) {
                predicates.add(cb.like(root.get("npcMember").get("name").as(String.class), "%" + performanceDto.getName() + "%"));
            }
            if (StringUtils.isNotEmpty(performanceDto.getMobile())){
                predicates.add(cb.like(root.get("npcMember").get("mobile").as(String.class), "%" + performanceDto.getMobile() + "%"));
            }
            //???????????? ??????
            if (performanceDto.getWorkAtStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), performanceDto.getWorkAtStart()));
            }
            if (performanceDto.getWorkAtEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), performanceDto.getWorkAtEnd()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return performancePage;
    }

    @Override
    public void exportPerformance(UserDetailsImpl userDetails, PerformanceDto performanceDto, HttpServletRequest req, HttpServletResponse res) {
        ServletOutputStream os = null;
        try {
            os = res.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Workbook hssWb = new HSSFWorkbook();
        int begin = performanceDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performanceDto.getSize(), Sort.Direction.fromString(performanceDto.getDirection()), performanceDto.getProperty());
        List<Performance> performances = this.getPerformancePage(userDetails,performanceDto, page).getContent();

        String fileName = ExcelCode.encodeFileName("??????????????????.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
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
        for (Performance performance : performances) {
            Row row = sheet.createRow(beginIndex);

            // ??????
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // ????????????
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(performance.getPerformanceType().getName());

            // ????????????
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(performance.getTitle());

            // ????????????
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(performance.getWorkAt().toString());

            // ????????????
            Cell cell4 = row.createCell(4);
            NpcMember member = performance.getNpcMember();
            if (member != null) {
                cell4.setCellValue(member.getName());
                // ????????????
                Cell cell7 = row.createCell(7);
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
            cell5.setCellValue(performance.getContent());

            //????????????
            Cell cell6 = row.createCell(6);
            String place = "";
            if (performance.getLevel().equals(LevelEnum.AREA.getValue())) {
                place = performance.getArea().getName() + performance.getTown().getName();
            } else if (performance.getLevel().equals(LevelEnum.TOWN.getValue())) {
                place = performance.getTown().getName();  // + performance.getNpcMemberGroup().getName();
            }
            cell6.setCellValue(place);

            // ?????????
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(performance.getAuditor() != null ? performance.getAuditor().getName():"");

            //????????????
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(PerformanceStatusEnum.getName(performance.getStatus()));

            //????????????
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(performance.getReason());

            //????????????
            Cell cell11 = row.createCell(11);
            cell11.setCellValue(simpleDateFormat.format(performance.getAuditAt()));

            //????????????
            Cell cell12 = row.createCell(12);
            cell12.setCellValue(LevelEnum.getName(performance.getLevel()));

        }
        try {
            hssWb.write(os);
            os.flush();
            os.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("???????????????????????? \n {}", e1);
        }
    }

    @Override
    public RespBody memberPerformanceCount(MemberCountDto dto, UserDetailsImpl userDetails) {
        RespBody<PageVo<MemberCountVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        Page<NpcMember> pageRes = this.getNpcMemberPage(dto,userDetails,page);//????????????????????????
        PageVo<MemberCountVo> vo = new PageVo<>(pageRes, dto);//??????????????????
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,pageRes);
        vo.setContent(vos);
        body.setData(vo);

        return body;
    }

    private List<MemberCountVo> getMemberCountVos(MemberCountDto dto, UserDetailsImpl userDetails, Page<NpcMember> pageRes){
        List<MemberCountVo> vos = Lists.newArrayList();
        List<NpcMember> content = pageRes.getContent();//????????????
        List<PerformanceType> performanceTypes = Lists.newArrayList();//?????????????????????????????????
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue())){
            performanceTypes = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(),userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypes = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }
        List<Performance> performanceList = this.getPerformanceList(dto,userDetails);//????????????????????????
        Map<String, Map<String,Integer>> memberPerformanceMap = this.dealPerformance(performanceList);//????????????????????????
        for (NpcMember npcMember : content) {//??????????????????
            MemberCountVo memberCountVo = new MemberCountVo();
            memberCountVo.setUid(npcMember.getUid());
            memberCountVo.setName(npcMember.getName());
            List<CountVo> countList = Lists.newArrayList();
            Map<String,Integer> countMap = memberPerformanceMap.getOrDefault(npcMember.getUid(),Maps.newHashMap());
            for (PerformanceType performanceType : performanceTypes) {//??????????????????????????????
                CountVo countVo = new CountVo();
                countVo.setUid(performanceType.getUid());
                countVo.setName(performanceType.getName());
                countVo.setCount(countMap.getOrDefault(performanceType.getUid(),0));
                countList.add(countVo);
            }
            memberCountVo.setCount(countList);
            vos.add(memberCountVo);
        }
        return vos;
    }


    @Override
    public void exportPerformanceCount(MemberCountDto dto, UserDetailsImpl userDetails, HttpServletRequest req, HttpServletResponse res) {
        ServletOutputStream os = null;
        try {
            os = res.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Workbook hssWb = new HSSFWorkbook();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        Page<NpcMember> pageRes = this.getNpcMemberPage(dto,userDetails,page);//????????????????????????
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,pageRes);//??????????????????
        // ?????????????????????????????????????????????????????????
        String fileName = ExcelCode.encodeFileName("??????????????????.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //??????Content-Disposition?????????????????????????????????????????????
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        List<PerformanceType> performanceTypes = Lists.newArrayList();//?????????????????????????????????
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypes = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            performanceTypes = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }
        String[] tableHeaders = new String[performanceTypes.size()+2];
        tableHeaders[0] = "??????";
        tableHeaders[1] = "??????";
        for (int i = 0; i < performanceTypes.size(); i++) {
            tableHeaders[i+2] = performanceTypes.get(i).getName();
        }
        Sheet sheet = hssWb.createSheet("??????????????????");
        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
        Integer[] total = new Integer[performanceTypes.size()];
        for (MemberCountVo memberCountVo : vos) {
            Row row = sheet.createRow(beginIndex);
            // ??????
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // ????????????
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(memberCountVo.getName());

            for (int i = 0; i < memberCountVo.getCount().size(); i++) {
                // ????????????
                Cell cell2 = row.createCell(i+2);
                cell2.setCellValue(memberCountVo.getCount().get(i).getCount());
                Integer number = total[i]==null?0:total[i];
                total[i] = number+memberCountVo.getCount().get(i).getCount();
            }
        }
        if (dto.getSize() == 9999){//??????????????????????????????
            Row row = sheet.createRow(beginIndex);
            // ??????
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(vos.size());

            // ????????????
            Cell cell1 = row.createCell(1);
            cell1.setCellValue("??????");

            for (int i = 0; i < total.length; i++) {
                Cell cell2 = row.createCell(i+2);
                cell2.setCellValue(total[i]==null?0:total[i]);
            }
        }
        try {
            hssWb.write(os);
            os.flush();
            os.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("???????????????????????? \n {}", e1);
        }
    }


    private Map<String, Map<String, Integer>> dealPerformance(List<Performance> performanceList) {
        Map<String, Map<String, Integer>> memberMaps = Maps.newHashMap();
        for (Performance performance : performanceList) {
            String memberUid = performance.getNpcMember().getUid();
            Map<String, Integer> countMap = memberMaps.getOrDefault(memberUid,Maps.newHashMap());
            countMap.put(performance.getPerformanceType().getUid(),countMap.getOrDefault(performance.getPerformanceType().getUid(),0)+1);
            memberMaps.put(memberUid,countMap);
        }
        return memberMaps;
    }

    private List<Performance> getPerformanceList(MemberCountDto dto, UserDetailsImpl userDetails) {
        List<Performance> performanceList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_SUCCESS.getValue()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //????????????
            if (StringUtils.isNotEmpty(dto.getName())) {
                predicates.add(cb.like(root.get("npcMember").get("name").as(String.class), "%" + dto.getName() + "%"));
            }
            //???????????? ??????
            if (dto.getStartAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), dto.getStartAt()));
            }
            if (dto.getEndAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), dto.getEndAt()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return performanceList;
    }


    private Page<NpcMember> getNpcMemberPage(MemberCountDto dto, UserDetailsImpl userDetails, Pageable page){
        Page<NpcMember> pageRes = npcMemberRepository.findAll((Specification<NpcMember>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //?????????bgAdmin???????????????
            predicateList.add(cb.equal(root.get("level"), userDetails.getLevel()));
            predicateList.add(cb.isFalse(root.get("isDel")));
            predicateList.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            predicateList.add(cb.equal(root.get("area").get("uid"), userDetails.getArea().getUid()));
            //??????????????? or ???????????????
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("town").get("uid"), userDetails.getTown().getUid()));
            }
            //?????????????????????
            if (StringUtils.isNotBlank(dto.getName())) {
                predicateList.add(cb.like(root.get("name"), "%" + dto.getName() + "%"));
            }
            //?????????????????????
            if (StringUtils.isNotEmpty(dto.getGroupId())) {
                String workUnit = "";
                if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                    workUnit = "npcMemberGroup";
                } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                    workUnit = "town";
                }
                predicateList.add(cb.equal(root.get(workUnit).get("uid"), dto.getGroupId()));
            }
            return cb.and(predicateList.toArray(new Predicate[0]));
        }, page);
        return pageRes;
    }

    /**
     * ?????????????????????
     *
     * @param performanceType
     * @param targetType
     * @return
     */
    private List<PerformanceType> changeSequence(PerformanceType performanceType, PerformanceType targetType) {
        List<PerformanceType> typeList = Lists.newArrayList();
        Integer beforeSec = performanceType.getSequence();
        performanceType.setSequence(targetType.getSequence());
        targetType.setSequence(beforeSec);
        typeList.add(performanceType);
        typeList.add(targetType);
        return typeList;
    }

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
