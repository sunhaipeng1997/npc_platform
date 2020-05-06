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
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {//镇上
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));//只查询所有镇、街道的履职
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                if (StringUtils.isNotEmpty(opinionPageDto.getUid())){//镇上按小组查询
                    predicates.add(cb.equal(root.get("receiver").get("npcMemberGroup").get("uid").as(String.class), opinionPageDto.getUid()));
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//如果是查询区上
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));//先按区筛选一次
                if (StringUtils.isNotEmpty(opinionPageDto.getUid())){//区上按镇查询、如果按镇筛选了，先筛选出来
                    predicates.add(cb.equal(root.get("receiver").get("town").get("uid").as(String.class), opinionPageDto.getUid()));
                }
                SystemSetting systemSetting = this.getSystemSetting(userDetails);
                if (systemSetting.getShowSubPerformance()) {//下级意见开关打开
                    List<NpcMember> areaMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),LevelEnum.AREA.getValue());//所有区代表
                    List<NpcMember> allMembers = Lists.newArrayList();//本次要查询的所有代表
                    for (NpcMember areaMember : areaMembers) {
                        if (areaMember.getAccount()!=null) {//注冊了小程序的代表
                            allMembers.addAll(areaMember.getAccount().getNpcMembers());
                        }else{//未注册的代表
                            allMembers.add(areaMember);
                        }
                    }
                    List<String> memberUid = Lists.newArrayList();//本次要查询的所有代表uid
                    for (NpcMember member : allMembers) {
                        memberUid.add(member.getUid());
                    }
                    if (CollectionUtils.isNotEmpty(memberUid)) {
                        predicates.add(cb.in(root.get("receiver").get("uid")).value(memberUid));
                    }
                }else{//开关关闭查询区上代表的身份收到的意见
                    predicates.add(cb.equal(root.get("receiver").get("level").as(Byte.class), LevelEnum.AREA.getValue()));
                }
//                Predicate predicateArea = cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue());//要么是在区上收到的意见查询出来
//                Predicate predicateTown = cb.and(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()),cb.equal(root.get("town").get("type").as(Byte.class), LevelEnum.AREA.getValue()));//要么就是街道的代表收到的意见查询出来
//                Predicate or = cb.or(predicateArea, predicateTown);
//                predicates.add(or);
            }
            //接受代表
            if (StringUtils.isNotEmpty(opinionPageDto.getMemberName())) {
                predicates.add(cb.like(root.get("receiver").get("name").as(String.class), "%" + opinionPageDto.getMemberName() + "%"));
            }
            if (StringUtils.isNotEmpty(opinionPageDto.getMobile())){
                predicates.add(cb.like(root.get("sender").get("mobile").as(String.class), "%" + opinionPageDto.getMobile() + "%"));
            }
            //提出时间 开始
            if (opinionPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), opinionPageDto.getDateStart()));
            }
            if (opinionPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), opinionPageDto.getDateEnd()));
            }
            return query.where(predicates.toArray(new javax.persistence.criteria.Predicate[0])).getRestriction();
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

        String fileName = ExcelCode.encodeFileName("意见信息.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //暴露Content-Disposition响应头，以便前端可以获取文件名
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        String[] tableHeaders = new String[]{"编号", "提出人", "提出时间", "提出人联系方式", "接收代表", "接收代表所属机构", "是否回复", "意见内容","意见所在行政等级"};
        Sheet sheet = hssWb.createSheet("意见信息");
        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
        for (Opinion opinion : opinions) {
            Row row = sheet.createRow(beginIndex);

            // 编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // 提出人
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(opinion.getSender().getVoter().getRealname());

            // 提出时间
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(opinion.getCreateTime());

            // 提出人联系方式
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(opinion.getSender().getVoter().getMobile());

            // 接收代表
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(opinion.getReceiver().getName());

            //接收代表所属机构
            Cell cell5 = row.createCell(5);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                cell5.setCellValue(opinion.getReceiver().getNpcMemberGroup().getName());
            }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                cell5.setCellValue(opinion.getReceiver().getTown().getName());
            }

            //是否回复
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(StatusEnum.getName(opinion.getStatus()));

            // 意见内容
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(opinion.getContent());

            // 意见所在行政等级
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(LevelEnum.getName(opinion.getLevel()));
        }
        try {
            hssWb.write(os);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("导出意见失败 \n {}", e1);
        }

    }

    @Override
    public RespBody deleteOpinion(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到意见信息！");
            return body;
        }
        Opinion opinion = opinionRepository.findByUid(uid);
        if (null == opinion){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到意见信息！");
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
