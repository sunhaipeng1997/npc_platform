package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.dto.OpinionPageDto;
import com.cdkhd.npc.entity.vo.OpinionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.member_house.OpinionRepository;
import com.cdkhd.npc.service.OpinionService;
import com.cdkhd.npc.util.ExcelCode;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpinionServiceImpl implements OpinionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpinionServiceImpl.class);

    private OpinionRepository opinionRepository;

    private NpcMemberRepository npcMemberRepository;

    private SystemSettingRepository systemSettingRepository;

    @Autowired
    public OpinionServiceImpl(OpinionRepository opinionRepository, NpcMemberRepository npcMemberRepository, SystemSettingRepository systemSettingRepository) {
        this.opinionRepository = opinionRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    public RespBody opinionPage(UserDetailsImpl userDetails, OpinionPageDto opinionPageDto) {

        RespBody body = new RespBody();
        int begin = opinionPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, opinionPageDto.getSize(), Sort.Direction.fromString(opinionPageDto.getDirection()), opinionPageDto.getProperty());
        Page<Opinion> opinionPage = this.getOpinionPage(userDetails,opinionPageDto,page);
        PageVo<OpinionVo> vo = new PageVo<>(opinionPage, opinionPageDto);
        List<OpinionVo> opinionVos = opinionPage.getContent().stream().map(OpinionVo::convert).collect(Collectors.toList());
        vo.setContent(opinionVos);
        body.setData(vo);

        return body;
    }

    private Page<Opinion> getOpinionPage(UserDetailsImpl userDetails, OpinionPageDto opinionPageDto, Pageable page) {
        Page<Opinion> opinionPage = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {//??????
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));//????????????????????????????????????
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                if (StringUtils.isNotEmpty(opinionPageDto.getUid())){//?????????????????????
                    predicates.add(cb.equal(root.get("receiver").get("npcMemberGroup").get("uid").as(String.class), opinionPageDto.getUid()));
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//?????????????????????
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));//?????????????????????
                if (StringUtils.isNotEmpty(opinionPageDto.getUid())){//????????????????????????????????????????????????????????????
                    predicates.add(cb.equal(root.get("receiver").get("town").get("uid").as(String.class), opinionPageDto.getUid()));
                }
                SystemSetting systemSetting = this.getSystemSetting(userDetails);
                if (systemSetting.getShowSubPerformance()) {//????????????????????????
                    List<NpcMember> areaMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());//???????????????
                    List<NpcMember> allMembers = Lists.newArrayList();//??????????????????????????????
                    for (NpcMember areaMember : areaMembers) {
                        if (areaMember.getAccount()!=null) {//???????????????????????????
                            allMembers.addAll(areaMember.getAccount().getNpcMembers());
                        }else{//??????????????????
                            allMembers.add(areaMember);
                        }
                    }
                    List<String> memberUid = Lists.newArrayList();//??????????????????????????????uid
                    for (NpcMember member : allMembers) {
                        memberUid.add(member.getUid());
                    }
                    if (CollectionUtils.isNotEmpty(memberUid)) {
                        predicates.add(cb.in(root.get("receiver").get("uid")).value(memberUid));
                    }
                }else{//??????????????????????????????????????????????????????
                    predicates.add(cb.equal(root.get("receiver").get("level").as(Byte.class), LevelEnum.AREA.getValue()));
                }
//                Predicate predicateArea = cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue());//?????????????????????????????????????????????
//                Predicate predicateTown = cb.and(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()),cb.equal(root.get("town").get("type").as(Byte.class), LevelEnum.AREA.getValue()));//??????????????????????????????????????????????????????
//                Predicate or = cb.or(predicateArea, predicateTown);
//                predicates.add(or);
            }
            //????????????
            if (StringUtils.isNotEmpty(opinionPageDto.getMemberName())) {
                predicates.add(cb.like(root.get("receiver").get("name").as(String.class), "%" + opinionPageDto.getMemberName() + "%"));
            }
            if (StringUtils.isNotEmpty(opinionPageDto.getMobile())){
                predicates.add(cb.like(root.get("sender").get("mobile").as(String.class), "%" + opinionPageDto.getMobile() + "%"));
            }
            //???????????? ??????
            if (opinionPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), opinionPageDto.getDateStart()));
            }
            if (opinionPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), opinionPageDto.getDateEnd()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return opinionPage;
    }

    @Override
    public void exportOpinion(UserDetailsImpl userDetails, OpinionPageDto opinionPageDto, HttpServletRequest req, HttpServletResponse res) {
        ServletOutputStream os = null;
        try {
            os = res.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Workbook hssWb = new HSSFWorkbook();
        int begin = opinionPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, opinionPageDto.getSize(), Sort.Direction.fromString(opinionPageDto.getDirection()), opinionPageDto.getProperty());
        List<Opinion> opinions = this.getOpinionPage(userDetails,opinionPageDto, page).getContent();

        String fileName = ExcelCode.encodeFileName("????????????.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //??????Content-Disposition?????????????????????????????????????????????
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        String[] tableHeaders = new String[]{"??????", "?????????", "????????????", "?????????????????????", "????????????","????????????", "????????????????????????", "????????????", "????????????","????????????"};
        Sheet sheet = hssWb.createSheet("????????????");
        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Opinion opinion : opinions) {
            Row row = sheet.createRow(beginIndex);

            // ??????
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // ?????????
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(opinion.getSender().getVoter().getRealname());

            // ????????????
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(simpleDateFormat.format(opinion.getCreateTime()));

            // ?????????????????????
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(opinion.getSender().getVoter().getMobile());

            // ????????????
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(opinion.getSender().getVoter().getArea().getName() +" "+opinion.getSender().getVoter().getTown().getName() +" "+opinion.getSender().getVoter().getVillage().getName());

            // ????????????
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(opinion.getReceiver().getName());

            //????????????????????????
            Cell cell6 = row.createCell(6);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                cell6.setCellValue(opinion.getReceiver().getNpcMemberGroup().getName());
            }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                cell6.setCellValue(opinion.getReceiver().getTown().getName());
            }

            //????????????
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(StatusEnum.getName(opinion.getStatus()));

            // ????????????
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(opinion.getContent());

            // ????????????????????????
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(LevelEnum.getName(opinion.getLevel()));
        }
        try {
            hssWb.write(os);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("?????????????????? \n {}", e1);
        }

    }

    @Override
    public RespBody deleteOpinion(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        Opinion opinion = opinionRepository.findByUid(uid);
        if (null == opinion){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        opinion.setIsDel(true);
        opinionRepository.saveAndFlush(opinion);
        return body;
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
