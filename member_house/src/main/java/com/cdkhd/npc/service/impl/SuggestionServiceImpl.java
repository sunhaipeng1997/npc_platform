package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.MemberCountDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessAddDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessDto;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.entity.vo.*;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.SuggestionService;
import com.cdkhd.npc.util.Constant;
import com.cdkhd.npc.util.ExcelCode;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
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
public class SuggestionServiceImpl implements SuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestionServiceImpl.class);

    private SuggestionBusinessRepository suggestionBusinessRepository;

    private SuggestionRepository suggestionRepository;

    private NpcMemberRepository npcMemberRepository;

    private SystemSettingRepository systemSettingRepository;

    private TownRepository townRepository;

    @Autowired
    public SuggestionServiceImpl(SuggestionBusinessRepository suggestionBusinessRepository, SuggestionRepository suggestionRepository, NpcMemberRepository npcMemberRepository, SystemSettingRepository systemSettingRepository, TownRepository townRepository) {
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionRepository = suggestionRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.townRepository = townRepository;
    }

    /**
     * ??????????????????????????????
     */
    @Override
    public RespBody sugBusList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> sb = Lists.newArrayList();
        //??????????????????????????????????????????
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))){
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(), userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody subTownBusList(String townUid) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> suggestionBusinesses = Lists.newArrayList();
        Town town = townRepository.findByUid(townUid);
        if (town != null && town.getType().equals(LevelEnum.AREA.getValue())){//???????????????????????????????????????????????????
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), town.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (StringUtils.isNotEmpty(townUid)){//?????????
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(), townUid, StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = suggestionBusinesses.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    /**
     * ????????????????????????
     *
     * @param userDetails
     * @param suggestionBusinessDto
     * @return
     */
    @Override
    public RespBody findSuggestionBusiness(UserDetailsImpl userDetails, SuggestionBusinessDto suggestionBusinessDto) {
        RespBody body = new RespBody();
        int begin = suggestionBusinessDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, suggestionBusinessDto.getSize(), Sort.Direction.fromString(suggestionBusinessDto.getDirection()), suggestionBusinessDto.getProperty());

        Page<SuggestionBusiness> suggestionBusinessPage = suggestionBusinessRepository.findAll((Specification<SuggestionBusiness>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            }
            if (suggestionBusinessDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), suggestionBusinessDto.getStatus()));
            }
            //???????????????
            if (StringUtils.isNotEmpty(suggestionBusinessDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + suggestionBusinessDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);

        PageVo<SuggestionBusinessVo> vo = new PageVo<>(suggestionBusinessPage, suggestionBusinessDto);
        List<SuggestionBusinessVo> suggestionBusinessVos = suggestionBusinessPage.getContent().stream().map(SuggestionBusinessVo::convert).collect(Collectors.toList());
        vo.setContent(suggestionBusinessVos);
        body.setData(vo);

        return body;
    }

    /**
     * ?????????????????????????????????
     *
     * @param userDetails
     * @param suggestionBusinessAddDto
     * @return
     */
    @Override
    public RespBody addOrUpdateSuggestionBusiness(UserDetailsImpl userDetails, SuggestionBusinessAddDto suggestionBusinessAddDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(suggestionBusinessAddDto.getName())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????");
            return body;
        }
        SuggestionBusiness suggestionBusiness = null;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionBusiness = suggestionBusinessRepository.findByNameAndLevelAndTownUidAndIsDelFalse(suggestionBusinessAddDto.getName(), userDetails.getLevel(), userDetails.getTown().getUid());
        }
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            suggestionBusiness = suggestionBusinessRepository.findByNameAndLevelAndAreaUidAndIsDelFalse(suggestionBusinessAddDto.getName(), userDetails.getLevel(), userDetails.getArea().getUid());
        }
        if (StringUtils.isEmpty(suggestionBusinessAddDto.getUid()) && suggestionBusiness != null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????");
            return body;
        }
        //??????uid?????????????????????????????????
        if (StringUtils.isNotEmpty(suggestionBusinessAddDto.getUid())) {
            suggestionBusiness = suggestionBusinessRepository.findByUid(suggestionBusinessAddDto.getUid());
            if (suggestionBusiness == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("???????????????????????????");
                return body;
            }
        } else {
            //uid???????????????????????????
            suggestionBusiness = new SuggestionBusiness();
            suggestionBusiness.setLevel(userDetails.getLevel());
            suggestionBusiness.setArea(userDetails.getArea());
            suggestionBusiness.setTown(userDetails.getTown());
            Integer maxSequence = 0;
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                Set<SuggestionBusiness> suggestionBusinesses = suggestionBusinessRepository.findByTownUid(userDetails.getTown().getUid());
                maxSequence = suggestionBusiness != null ? suggestionBusinesses.size() + 1 : 1;
            }if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                Set<SuggestionBusiness> suggestionBusinesses = suggestionBusinessRepository.findByAreaUidAndLevel(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
                maxSequence = suggestionBusiness != null ? suggestionBusinesses.size() + 1 : 1;
            }
            suggestionBusiness.setSequence(maxSequence);
        }
        suggestionBusiness.setName(suggestionBusinessAddDto.getName());
        suggestionBusiness.setRemark(suggestionBusinessAddDto.getRemark());

        suggestionBusinessRepository.saveAndFlush(suggestionBusiness);
        return body;
    }

    /**
     * ??????????????????
     *
     * @param uid
     * @return
     */
    @Override
    public RespBody deleteSuggestionBusiness(String uid) {
        RespBody body = new RespBody();
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(uid);
        if (suggestionBusiness == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        suggestionBusiness.setIsDel(true);
        suggestionBusinessRepository.saveAndFlush(suggestionBusiness);
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
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(uid);
        if (suggestionBusiness == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        SuggestionBusiness target = null;
        if (type.equals(Constant.LOGIN_WAY_UP)) {//1 ??????
            Sort sort = new Sort(Sort.Direction.DESC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                target = suggestionBusinessRepository.findBySequenceAndLevelAreaUidDesc(suggestionBusiness.getSequence(), userDetails.getLevel(), userDetails.getArea().getUid(), page).getContent().get(0);
            } else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                target = suggestionBusinessRepository.findBySequenceAndLevelTownUidDesc(suggestionBusiness.getSequence(), userDetails.getLevel(), userDetails.getTown().getUid(), page).getContent().get(0);
            }
        } else {
            Sort sort = new Sort(Sort.Direction.ASC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                target = suggestionBusinessRepository.findBySequenceAndLevelTownUidAsc(suggestionBusiness.getSequence(), userDetails.getLevel(), userDetails.getTown().getUid(), page).getContent().get(0);
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                target = suggestionBusinessRepository.findBySequenceAndLevelAreaUidAsc(suggestionBusiness.getSequence(), userDetails.getLevel(), userDetails.getArea().getUid(), page).getContent().get(0);
            }
        }
        if (target == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????");
            return body;
        }
//        if (!type.equals(Constant.LOGIN_WAY_UP)) {//1 ??????
//            Sort sort = new Sort(Sort.Direction.ASC, "sequence");
//            Pageable page = PageRequest.of(0, 1, sort);
//            target = suggestionBusinessRepository.findBySequenceAsc(suggestionBusiness.getSequence(), page).getContent().get(0);
//        } else {
//            Sort sort = new Sort(Sort.Direction.DESC, "sequence");
//            Pageable page = PageRequest.of(0, 1, sort);
//            target = suggestionBusinessRepository.findBySequenceDesc(suggestionBusiness.getSequence(), page).getContent().get(0);
//        }
        List<SuggestionBusiness> sb = this.changeSequence(suggestionBusiness, target);
        suggestionBusinessRepository.saveAll(sb);
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
    public RespBody changeBusinessStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(uid);
        if (suggestionBusiness == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        suggestionBusiness.setStatus(status);
        suggestionBusinessRepository.saveAndFlush(suggestionBusiness);
        return body;
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @Override
    public RespBody findSuggestion(UserDetailsImpl userDetails, SuggestionDto suggestionDto) {
        RespBody body = new RespBody();
        //???????????????????????????????????????????????????
        int begin = suggestionDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, suggestionDto.getSize(), Sort.Direction.fromString(suggestionDto.getDirection()), suggestionDto.getProperty());
        Page<Suggestion> suggestionPage = this.getSuggestionPage(userDetails, suggestionDto, page);
        PageVo<SuggestionVo> vo = new PageVo<>(suggestionPage, suggestionDto);
        List<SuggestionVo> suggestionVos = suggestionPage.getContent().stream().map(SuggestionVo::convert).collect(Collectors.toList());
        vo.setContent(suggestionVos);
        body.setData(vo);
        return body;
    }

    /**
     * ??????????????????
     *
     * @param userDetails
     * @param suggestionDto
     * @return
     */
    @Override
    public void exportSuggestion(UserDetailsImpl userDetails, SuggestionDto suggestionDto, HttpServletRequest req, HttpServletResponse res) {
        ServletOutputStream os = null;
        try {
            os = res.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Workbook hssWb = new HSSFWorkbook();
        int begin = suggestionDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, suggestionDto.getSize(), Sort.Direction.fromString(suggestionDto.getDirection()), suggestionDto.getProperty());
        List<Suggestion> suggestions = this.getSuggestionPage(userDetails, suggestionDto, page).getContent();

        String fileName = ExcelCode.encodeFileName("????????????????????????.xls", req);
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

    @Override
    public RespBody memberSuggestionCount(MemberCountDto dto, UserDetailsImpl userDetails) {
        RespBody<PageVo<MemberCountVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        Page<NpcMember> pageRes = this.getNpcMemberPage(dto,userDetails,page);//????????????????????????
        PageVo<MemberCountVo> vo = new PageVo<>(pageRes, dto);//??????????????????
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,page,pageRes);
        vo.setContent(vos);
        body.setData(vo);
        return body;
    }

    private List<MemberCountVo> getMemberCountVos(MemberCountDto dto, UserDetailsImpl userDetails, Pageable page, Page<NpcMember> pageRes){
        List<NpcMember> content = pageRes.getContent();//????????????
        List<MemberCountVo> vos = Lists.newArrayList();
        List<SuggestionBusiness> suggestionBusinesses = Lists.newArrayList();//?????????????????????????????????
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue())){
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(),userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }
        List<Suggestion> suggestionList = this.getSuggestionList(dto,userDetails);//????????????????????????
        Map<String, Map<String,Integer>> memberSuggestionMap = this.dealSuggestion(suggestionList);//????????????????????????
        for (NpcMember npcMember : content) {//??????????????????
            MemberCountVo memberCountVo = new MemberCountVo();
            memberCountVo.setUid(npcMember.getUid());
            memberCountVo.setName(npcMember.getName());
            List<CountVo> countList = Lists.newArrayList();
            Map<String,Integer> countMap = memberSuggestionMap.getOrDefault(npcMember.getUid(), Maps.newHashMap());
            for (SuggestionBusiness suggestionBusiness : suggestionBusinesses) {//?????????????????????????????????
                CountVo countVo = new CountVo();
                countVo.setUid(suggestionBusiness.getUid());
                countVo.setName(suggestionBusiness.getName());
                countVo.setCount(countMap.getOrDefault(suggestionBusiness.getUid(),0));
                countList.add(countVo);
            }
            memberCountVo.setCount(countList);
            vos.add(memberCountVo);
        }
        return vos;
    }


    /**
     * ??????????????????
     *
     * @param userDetails
     * @param dto
     * @return
     */
    @Override
    public void exportSuggestionCount(MemberCountDto dto, UserDetailsImpl userDetails, HttpServletRequest req, HttpServletResponse res) {
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
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,page, pageRes);
        String fileName = ExcelCode.encodeFileName("??????????????????.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //??????Content-Disposition?????????????????????????????????????????????
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        List<SuggestionBusiness> suggestionBusinesses = Lists.newArrayList();//?????????????????????????????????
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }

        String[] tableHeaders = new String[suggestionBusinesses.size()+2];
        tableHeaders[0] = "??????";
        tableHeaders[1] = "??????";
        for (int i = 0; i < suggestionBusinesses.size(); i++) {
            tableHeaders[i+2] = suggestionBusinesses.get(i).getName();
        }
        Sheet sheet = hssWb.createSheet("??????????????????");
        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }
        int beginIndex = 1;
        Integer[] total = new Integer[suggestionBusinesses.size()];
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
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("???????????????????????? \n {}", e1);
        }
    }


    private Map<String, Map<String, Integer>> dealSuggestion(List<Suggestion> suggestionList) {
        Map<String, Map<String, Integer>> memberMaps = Maps.newHashMap();
        for (Suggestion suggestion : suggestionList) {
            String memberUid = suggestion.getRaiser().getUid();
            Map<String, Integer> countMap = memberMaps.getOrDefault(memberUid,Maps.newHashMap());
            countMap.put(suggestion.getSuggestionBusiness().getUid(),countMap.getOrDefault(suggestion.getSuggestionBusiness().getUid(),0)+1);
            memberMaps.put(memberUid,countMap);
        }
        return memberMaps;
    }

    private List<Suggestion> getSuggestionList(MemberCountDto dto, UserDetailsImpl userDetails) {
        List<Suggestion> suggestionList = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            Predicate or = cb.or(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SELF_HANDLE.getValue()),cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));
            predicates.add(or);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //????????????
            if (StringUtils.isNotEmpty(dto.getName())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + dto.getName() + "%"));
            }
            //?????????????????? ??????
            if (dto.getStartAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("raiseTime").as(Date.class), dto.getStartAt()));
            }
            if (dto.getEndAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("raiseTime").as(Date.class), dto.getEndAt()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return suggestionList;
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
     * @param suggestionBusiness
     * @param target
     * @return
     */
    private List<SuggestionBusiness> changeSequence(SuggestionBusiness suggestionBusiness, SuggestionBusiness target) {
        List<SuggestionBusiness> typeList = com.google.common.collect.Lists.newArrayList();
        Integer beforeSec = suggestionBusiness.getSequence();
        suggestionBusiness.setSequence(target.getSequence());
        target.setSequence(beforeSec);
        typeList.add(suggestionBusiness);
        typeList.add(target);
        return typeList;
    }

    private Page<Suggestion> getSuggestionPage(UserDetailsImpl userDetails, SuggestionDto suggestionDto, Pageable page) {
        Page<Suggestion> suggestionPage = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));

            Predicate predicate =cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SELF_HANDLE.getValue());
            predicates.add(predicate);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));//?????????????????????????????????????????????
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//???????????????????????????
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (!suggestionDto.isFlag()){//??????????????????????????????level?????????????????????
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                }else {//??????????????????????????????
                    SystemSetting systemSetting = this.getSystemSetting(userDetails);//????????????????????????
                    if (systemSetting.getShowSubPerformance()) {//????????????????????????
                        //?????????????????????????????????
                        List<NpcMember> areaMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
                        List<NpcMember> allMembers = Lists.newArrayList();//????????????????????????????????????????????????????????????????????????????????????
                        for (NpcMember areaMember : areaMembers) {
                            if (areaMember.getAccount()!=null) {//??????????????????
                                allMembers.addAll(areaMember.getAccount().getNpcMembers());
                            }else{//??????????????????
                                allMembers.add(areaMember);
                            }
                        }
                        List<String> memberUid = Lists.newArrayList();
                        for (NpcMember member : allMembers) {
                            memberUid.add(member.getUid());//????????????????????????????????????uid
                        }
                        if (CollectionUtils.isNotEmpty(memberUid)) {
                            predicates.add(cb.in(root.get("raiser").get("uid")).value(memberUid));
                        }
                    }else{//??????????????????????????????????????????????????????????????????
                        predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
                    }
                }
            }
            //??????
            if (StringUtils.isNotEmpty(suggestionDto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + suggestionDto.getTitle() + "%"));
            }
            //?????????
            if (!suggestionDto.isFlag() && userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                if ( StringUtils.isNotEmpty(suggestionDto.getTownUid()) ){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), suggestionDto.getTownUid()));
                }
            }
            //??????
            if (StringUtils.isNotEmpty(suggestionDto.getSuggestionBusiness())) {
                predicates.add(cb.equal(root.get("suggestionBusiness").get("uid").as(String.class), suggestionDto.getSuggestionBusiness()));
            }
            //????????????
            if (StringUtils.isNotEmpty(suggestionDto.getName())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + suggestionDto.getName() + "%"));
            }
            if (StringUtils.isNotEmpty(suggestionDto.getMobile())) {
                predicates.add(cb.equal(root.get("raiser").get("mobile").as(String.class), suggestionDto.getMobile()));
            }
            //???????????? ??????
            if (suggestionDto.getAuditStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), suggestionDto.getAuditStart()));
            }
            if (suggestionDto.getAuditEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), suggestionDto.getAuditEnd()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return suggestionPage;
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
