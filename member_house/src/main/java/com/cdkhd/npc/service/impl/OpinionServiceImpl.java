package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.entity.dto.OpinionPageDto;
import com.cdkhd.npc.entity.vo.OpinionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.member_house.OpinionRepository;
import com.cdkhd.npc.service.OpinionService;
import com.cdkhd.npc.util.ExcelCode;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
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

    @Autowired
    public OpinionServiceImpl(OpinionRepository opinionRepository) {
        this.opinionRepository = opinionRepository;
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
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                if (StringUtils.isNotEmpty(opinionPageDto.getUid())){
                    predicates.add(cb.equal(root.get("receiver").get("npcMemberGroup").get("uid").as(String.class), opinionPageDto.getUid()));
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (StringUtils.isNotEmpty(opinionPageDto.getUid())){
                    predicates.add(cb.equal(root.get("receiver").get("town").get("uid").as(String.class), opinionPageDto.getUid()));
                }

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
        // 查询还利息或者还款日期在这段时间的数据
        String fileName = ExcelCode.encodeFileName("意见信息.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
        String[] tableHeaders = new String[]{"编号", "提出人", "提出时间", "提出人联系方式", "接收代表", "接收代表所属机构", "是否回复", "意见内容"};
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
}
