package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.StatisticalPageDto;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.StatisticalService;
import com.cdkhd.npc.util.ExcelCode;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StatisticalServiceImpl implements StatisticalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticalServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private NpcMemberRepository npcMemberRepository;

    private SystemSettingRepository systemSettingRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    private TownRepository townRepository;

    @Autowired
    public StatisticalServiceImpl(PerformanceRepository performanceRepository, NpcMemberRepository npcMemberRepository, SystemSettingRepository systemSettingRepository, PerformanceTypeRepository performanceTypeRepository, TownRepository townRepository) {
        this.performanceRepository = performanceRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.townRepository = townRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.performanceTypeRepository = performanceTypeRepository;
    }

    /**
     * ??????????????????
     * @param userDetails
     * @param statisticalPageDto
     * @return
     */
    @Override
    public RespBody memberPerformance(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        RespBody body = new RespBody();
        //???????????????????????????????????????????????????
        int begin = statisticalPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, statisticalPageDto.getSize(), Sort.Direction.fromString(statisticalPageDto.getDirection()), statisticalPageDto.getProperty());
        Page<NpcMember> npcMembers = this.getNpcMemberPage(userDetails,statisticalPageDto,page);
        List<Performance> performanceList = this.getPerformanceList(userDetails,statisticalPageDto);
        List<PerformanceType> performanceTypeList = this.getPerformanceTypeList(userDetails);
        Map<String,Map<String, Integer>> memberMap = this.dealPerformanceForMember(npcMembers,performanceList,performanceTypeList);
        body.setData(memberMap);
        return body;
    }



    private Map<String, Map<String, Integer>> dealPerformanceForMember(Page<NpcMember> npcMembers, List<Performance> performanceList, List<PerformanceType> performanceTypeList) {
        Map<String, Map<String, Integer>> memberNameMap = Maps.newHashMap();// key:????????????  value:Map<Map:????????????, Integer:????????????>
        Map<String, Map<String, Integer>> memberMap = Maps.newHashMap();// key:??????uid  value:Map<Map:????????????, Integer:????????????>
        Map<String, String> members = Maps.newHashMap();//// key?????????uid  value???????????????    //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????uid??????????????????????????????????????????????????????
        Map<String, Integer> performanceMap = Maps.newHashMap();
        for (PerformanceType performanceType : performanceTypeList) {//???????????????????????????????????????0???
            performanceMap.put(performanceType.getUid(),0);
        }
        for (NpcMember npcMember : npcMembers) {//?????????????????????????????????
            members.put(npcMember.getUid(),npcMember.getName());
            memberMap.put(npcMember.getUid(),performanceMap);
        }
        for (Performance performance : performanceList) {//?????????????????????????????????????????????
            String npcMemberUid = performance.getNpcMember().getUid();//??????uid
            String performanceUid = performance.getPerformanceType().getUid();//????????????uid
            Map<String, Integer> performances = memberMap.getOrDefault(npcMemberUid,Maps.newHashMap());//??????????????????????????????????????????
            Integer times = performances.getOrDefault(performanceUid,0)+1;
            performanceMap.put(performanceMap.keySet().toString(),times);
            memberMap.put(npcMemberUid,performances);
        }
        for (String s : memberMap.keySet()) {
            memberNameMap.put(members.get(s),memberMap.get(s));
        }
        return memberMap;
    }

    /**
     * ?????????????????????????????????
     * @param userDetails
     * @return
     */
    private List<PerformanceType> getPerformanceTypeList(UserDetailsImpl userDetails) {
        List<PerformanceType> performanceTypeList = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue())){
            performanceTypeList = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(),userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
            performanceTypeList = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }
        return performanceTypeList;
    }

    /**
     * ??????????????????
     * @param userDetails
     * @param statisticalPageDto
     * @param page
     * @return
     */
    private Page<NpcMember> getNpcMemberPage(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, Pageable page) {
        Page<NpcMember> npcMemberPage = npcMemberRepository.findAll((Specification<NpcMember>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                //????????????
                if (StringUtils.isNotEmpty(statisticalPageDto.getAreaUId())) {
                    predicates.add(cb.equal(root.get("npcMemberGroup").get("uid").as(String.class), statisticalPageDto.getAreaUId()));
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                //????????????
                if (StringUtils.isNotEmpty(statisticalPageDto.getAreaUId())) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), statisticalPageDto.getAreaUId()));
                }
            }
            //????????????
            if (StringUtils.isNotEmpty(statisticalPageDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + statisticalPageDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        },page);
        return npcMemberPage;
    }

    /**
     * ??????????????????
     * @param userDetails
     * @param statisticalPageDto
     * @return
     */
    private List<Performance> getPerformanceList(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        List<Performance> performanceList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                SystemSetting systemSetting = this.getSystemSetting(userDetails);
                if (systemSetting.getShowSubPerformance()) {
                    List<NpcMember> members = npcMemberRepository.findByLevel(LevelEnum.TOWN.getValue());
                    List<String> accountUids = Lists.newArrayList();
                    for (NpcMember member : members) {
                        accountUids.add(member.getAccount().getUid());
                    }
                    predicates.add(cb.in(root.get("npcMember").get("account").get("uid")).value(accountUids));
                }
            }
            //????????????
            if (StringUtils.isNotEmpty(statisticalPageDto.getName())) {
                predicates.add(cb.like(root.get("npcMember").get("name").as(String.class), "%" + statisticalPageDto.getName() + "%"));
            }
            //???????????? ??????
            if (statisticalPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), statisticalPageDto.getDateStart()));
            }
            if (statisticalPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), statisticalPageDto.getDateEnd()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return performanceList;
    }


    /**
     * ??????????????????
     * @param userDetails
     * @param statisticalPageDto
     * @return
     */
    @Override
    public RespBody townPerformance(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        RespBody body = new RespBody();
        //???????????????????????????????????????????????????
        int begin = statisticalPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, statisticalPageDto.getSize(), Sort.Direction.fromString(statisticalPageDto.getDirection()), statisticalPageDto.getProperty());
        Page<Town> towns = this.getTownPage(userDetails,statisticalPageDto,page);
        List<Performance> performanceList = this.getPerformanceList(userDetails,statisticalPageDto);
        List<PerformanceType> performanceTypeList = this.getPerformanceTypeList(userDetails);
        Map<String,Map<String, Integer>> townMap = this.dealPerformance(towns,performanceList,performanceTypeList);
        body.setData(townMap);
        return body;
    }

    @Override
    public void exportStatistical(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, HttpServletRequest req, HttpServletResponse res) {
        ServletOutputStream os = null;
        try {
            os = res.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Workbook hssWb = new HSSFWorkbook();
        int begin = statisticalPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, statisticalPageDto.getSize(), Sort.Direction.fromString(statisticalPageDto.getDirection()), statisticalPageDto.getProperty());
        Page<NpcMember> npcMembers = this.getNpcMemberPage(userDetails,statisticalPageDto,page);
        List<Performance> performanceList = this.getPerformanceList(userDetails,statisticalPageDto);
        List<PerformanceType> performanceTypeList = this.getPerformanceTypeList(userDetails);
        Map<String,Map<String, Integer>> memberMap = this.dealPerformanceForMember(npcMembers,performanceList,performanceTypeList);

        String fileName = ExcelCode.encodeFileName("??????????????????.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");

        //todo  ??????????????????
        String[] tableHeaders = new String[]{"??????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "?????????", "????????????", "????????????"};

        Sheet sheet = hssWb.createSheet("????????????");

        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
//        for (Suggestion suggestion : suggestions) {
//            Row row = sheet.createRow(beginIndex);
//
//            // ??????
//            Cell cell0 = row.createCell(0);
//            cell0.setCellValue(beginIndex);
//            beginIndex++;
//
//            // ????????????
//            Cell cell1 = row.createCell(1);
//            cell1.setCellValue(suggestion.getSuggestionBusiness().getName());
//
//            // ????????????
//            Cell cell2 = row.createCell(2);
//            cell2.setCellValue(suggestion.getTitle());
//
//            // ????????????
//            Cell cell3 = row.createCell(3);
//            cell3.setCellValue(suggestion.getAuditTime());
//
//            // ????????????
//            Cell cell4 = row.createCell(4);
//            NpcMember member = suggestion.getRaiser();
//            if (member != null) {
//                // ????????????
//                Cell cell7 = row.createCell(7);
//                cell4.setCellValue(member.getName());
//                String mobile = member.getMobile();
//                if (StringUtils.isNotBlank(mobile)) {
//                    cell7.setCellValue(mobile);
//                } else {
//                    cell7.setCellValue("");
//                }
//            } else {
//                cell2.setCellValue("");
//            }
//
//            //????????????
//            Cell cell5 = row.createCell(5);
//            cell5.setCellValue(suggestion.getContent());
//
//            //????????????
//            Cell cell6 = row.createCell(6);
//            String place = "";
//            if (suggestion.getLevel().equals(LevelEnum.AREA.getValue())) {
//                place = suggestion.getArea().getName() + suggestion.getTown().getName();
//            } else if (suggestion.getLevel().equals(LevelEnum.TOWN.getValue())) {
//                place = suggestion.getTown().getName() + suggestion.getNpcMemberGroup().getName();
//            }
//            cell6.setCellValue(place);
//
//            // ?????????
//            Cell cell7 = row.createCell(7);
//            cell7.setCellValue(suggestion.getAuditor().getName());
//
//            //????????????
//            Cell cell8 = row.createCell(8);
//            cell8.setCellValue(suggestion.getStatus());
//
//            //????????????
//            Cell cell9 = row.createCell(9);
//            cell9.setCellValue(suggestion.getReason());
//
//        }
        try {
            hssWb.write(os);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("???????????????????????? \n {}", e1);
        }
    }

    @Override
    public void exportTownStatistical(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, HttpServletRequest req, HttpServletResponse res) {

    }

    private Map<String, Map<String, Integer>> dealPerformance(Page<Town> towns, List<Performance> performanceList, List<PerformanceType> performanceTypeList) {
        Map<String, Map<String, Integer>> townNameMap = Maps.newHashMap();// key:?????????  value:Map<Map:????????????, Integer:????????????>
        Map<String, Map<String, Integer>> townMap = Maps.newHashMap();// key:???uid  value:Map<Map:????????????, Integer:????????????>
        Map<String, String> townInfo = Maps.newHashMap();//// key??????uid  value????????????    //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????uid??????????????????????????????????????????????????????
        Map<String, Integer> performanceMap = Maps.newHashMap();
        for (PerformanceType performanceType : performanceTypeList) {//???????????????????????????????????????0???
            performanceMap.put(performanceType.getUid(),0);
        }
        for (Town town : towns) {//?????????????????????????????????
            townInfo.put(town.getUid(),town.getName());
            townMap.put(town.getUid(),performanceMap);
        }
        for (Performance performance : performanceList) {//?????????????????????????????????????????????
            String townUid = performance.getTown().getUid();//??????uid
            String performanceUid = performance.getPerformanceType().getUid();//????????????uid
            Map<String, Integer> performances = townMap.getOrDefault(townUid,Maps.newHashMap());//??????????????????????????????????????????
            Integer times = performances.getOrDefault(performanceUid,0)+1;
            performanceMap.put(performanceMap.keySet().toString(),times);
            townMap.put(townUid,performances);
        }
        for (String s : townMap.keySet()) {
            townNameMap.put(townInfo.get(s),townMap.get(s));
        }
        return townMap;
    }


    private Page<Town> getTownPage(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, Pageable page) {
        Page<Town> townPage = townRepository.findAll((Specification<Town>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            //?????????
            if (StringUtils.isNotEmpty(statisticalPageDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + statisticalPageDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        },page);
        return townPage;
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
