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
     * 条件查询履职类型
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
            //按名称查询
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
     * 添加或修改履职类型
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
            body.setMessage("类型名称不能为空！");
            return body;
        }
        PerformanceType performanceType = null;
        if (StringUtils.isEmpty(performanceTypeAddDto.getUid())) {//添加验证重复
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndTownUidAndIsDelFalse(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getTown().getUid());
            }if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndAreaUidAndIsDelFalse(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getArea().getUid());
            }
        }else{//修改验证重复
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndTownUidAndIsDelFalseAndUidIsNot(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getTown().getUid(),performanceTypeAddDto.getUid());
            }if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                performanceType = performanceTypeRepository.findByNameAndLevelAndAreaUidAndIsDelFalseAndUidIsNot(performanceTypeAddDto.getName(),userDetails.getLevel(),userDetails.getArea().getUid(),performanceTypeAddDto.getUid());
            }
        }

        if (performanceType != null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("类型名称已经存在！");
            return body;
        }
        //如果uid不为空，说明是修改操作
        if (StringUtils.isNotEmpty(performanceTypeAddDto.getUid())) {
            performanceType = performanceTypeRepository.findByUid(performanceTypeAddDto.getUid());
            if (performanceType == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("履职类型信息有误！");
                return body;
            }
        } else {
            //uid为空说明是添加操作
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
            if(maxSequence == null){//防治数据库初始为空时报错，所以将初始序号设置为0。（李亚林）
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
     * 删除履职类型
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
            body.setMessage("找不到履职类型！");
            return body;
        }
        if (performanceType.getIsDefault()) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该履职类型不允许删除！");
            return body;
        }
        performanceType.setIsDel(true);
        performanceTypeRepository.saveAndFlush(performanceType);
        return body;
    }

    /**
     * 调整类型顺序
     *
     * @param uid  类型uid
     * @param type 上移  下移
     * @return
     */
    @Override
    public RespBody changeTypeSequence(UserDetailsImpl userDetails, String uid, Byte type) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到履职类型！");
            return body;
        }
        PerformanceType targetType = null;
        if (type.equals(Constant.LOGIN_WAY_UP)) {//1 上移
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
            body.setMessage("移动失败！");
            return body;
        }
        List<PerformanceType> types = this.changeSequence(performanceType, targetType);
        performanceTypeRepository.saveAll(types);
        return body;
    }

    /**
     * 修改履职类型状态
     *
     * @param uid
     * @param status 1 开启 2 关闭
     * @return
     */
    @Override
    public RespBody changeTypeStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到履职类型！");
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
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypes = performanceTypeRepository.findByLevelAndTownUidAndIsDelFalse(userDetails.getLevel(),userDetails.getTown().getUid());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            performanceTypes = performanceTypeRepository.findByLevelAndAreaUidAndIsDelFalse(userDetails.getLevel(),userDetails.getArea().getUid());
        }
        List<CommonVo> commonVos = performanceTypes.stream().map(type -> CommonVo.convert(type.getUid(),type.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }


    @Override
    public RespBody subTownPerformanceTypeList(String townUid) {
        RespBody body = new RespBody();
        List<PerformanceType> sb = Lists.newArrayList();
        if (StringUtils.isNotEmpty(townUid)){
            sb = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(), townUid, StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody findPerformance(UserDetailsImpl userDetails, PerformanceDto performanceDto) {
        RespBody body = new RespBody();
        //查询代表的履职之前首先查询系统配置
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
            body.setMessage("找不到履职信息！");
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
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                SystemSetting systemSetting = this.getSystemSetting(userDetails);
                if (systemSetting.getShowSubPerformance()) {
                    List<NpcMember> areaMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
                    List<NpcMember> allMembers = Lists.newArrayList();
                    for (NpcMember areaMember : areaMembers) {
                        if (areaMember.getAccount()!=null) {//注冊了的代表
                            allMembers.addAll(areaMember.getAccount().getNpcMembers());
                        }else{//未注册的代表
                            allMembers.add(areaMember);
                        }
                    }
                    List<String> memberUid = Lists.newArrayList();
                    for (NpcMember member : areaMembers) {
                        memberUid.add(member.getUid());
                    }
                    if (CollectionUtils.isNotEmpty(memberUid)) {
                        predicates.add(cb.in(root.get("npcMember").get("uid")).value(memberUid));
                    }
                }
            }
            //标题
            if (StringUtils.isNotEmpty(performanceDto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + performanceDto.getTitle() + "%"));
            }
            //类型
            if (StringUtils.isNotEmpty(performanceDto.getPerformanceType())) {
                predicates.add(cb.equal(root.get("performanceType").get("uid").as(String.class), performanceDto.getPerformanceType()));
            }
            //提出代表
            if (StringUtils.isNotEmpty(performanceDto.getName())) {
                predicates.add(cb.like(root.get("npcMember").get("name").as(String.class), "%" + performanceDto.getName() + "%"));
            }
            if (StringUtils.isNotEmpty(performanceDto.getMobile())){
                predicates.add(cb.like(root.get("npcMember").get("mobile").as(String.class), "%" + performanceDto.getMobile() + "%"));
            }
            //履职时间 开始
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

        String fileName = ExcelCode.encodeFileName("代表履职信息.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //暴露Content-Disposition响应头，以便前端可以获取文件名
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        String[] tableHeaders = new String[]{"编号", "履职类型", "履职标题", "履职时间", "履职代表", "履职内容", "所属地区", "联系方式", "审核人", "审核状态", "审核意见"};

        Sheet sheet = hssWb.createSheet("代表履职");

        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
        for (Performance performance : performances) {
            Row row = sheet.createRow(beginIndex);

            // 编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // 履职类型
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(performance.getPerformanceType().getName());

            // 履职标题
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(performance.getTitle());

            // 履职时间
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(performance.getWorkAt().toString());

            // 履职代表
            Cell cell4 = row.createCell(4);
            NpcMember member = performance.getNpcMember();
            if (member != null) {
                cell4.setCellValue(member.getName());
                // 联系方式
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

            //履职内容
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(performance.getContent());

            //所属地区
            Cell cell6 = row.createCell(6);
            String place = "";
            if (performance.getLevel().equals(LevelEnum.AREA.getValue())) {
                place = performance.getArea().getName() + performance.getTown().getName();
            } else if (performance.getLevel().equals(LevelEnum.TOWN.getValue())) {
                place = performance.getTown().getName();  // + performance.getNpcMemberGroup().getName();
            }
            cell6.setCellValue(place);

            // 审核人
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(performance.getAuditor().getName());

            //审核状态
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(StatusEnum.getName(performance.getStatus()));

            //审核意见
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(performance.getReason());

        }
        try {
            hssWb.write(os);
            os.flush();
            os.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("导出代表履职失败 \n {}", e1);
        }
    }

    @Override
    public RespBody memberPerformanceCount(MemberCountDto dto, UserDetailsImpl userDetails) {
        RespBody<PageVo<MemberCountVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        Page<NpcMember> pageRes = this.getNpcMemberPage(dto,userDetails,page);//分页获取所有代表
        PageVo<MemberCountVo> vo = new PageVo<>(pageRes, dto);//代表分页对象
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,pageRes);
        vo.setContent(vos);
        body.setData(vo);

        return body;
    }

    private List<MemberCountVo> getMemberCountVos(MemberCountDto dto, UserDetailsImpl userDetails, Page<NpcMember> pageRes){
        List<MemberCountVo> vos = Lists.newArrayList();
        List<NpcMember> content = pageRes.getContent();//代表列表
        List<PerformanceType> performanceTypes = Lists.newArrayList();//获取所有可用的履职类型
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypes = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            performanceTypes = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
        }
        List<Performance> performanceList = this.getPerformanceList(dto,userDetails);//获取所有履职信息
        Map<String, Map<String,Integer>> memberPerformanceMap = this.dealPerformance(performanceList);//处理所有履职信息
        for (NpcMember npcMember : content) {//遍历代表信息
            MemberCountVo memberCountVo = new MemberCountVo();
            memberCountVo.setUid(npcMember.getUid());
            memberCountVo.setName(npcMember.getName());
            List<CountVo> countList = Lists.newArrayList();
            Map<String,Integer> countMap = memberPerformanceMap.getOrDefault(npcMember.getUid(),Maps.newHashMap());
            for (PerformanceType performanceType : performanceTypes) {//遍历所有铝箔纸类型信息
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
        Page<NpcMember> pageRes = this.getNpcMemberPage(dto,userDetails,page);//分页获取所有代表
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,pageRes);//履职统计包装
        // 查询还利息或者还款日期在这段时间的数据
        String fileName = ExcelCode.encodeFileName("代表履职统计.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //暴露Content-Disposition响应头，以便前端可以获取文件名
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        List<PerformanceType> performanceTypes = Lists.newArrayList();//获取所有可用的履职类型
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypes = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            performanceTypes = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
        }
        String[] tableHeaders = new String[performanceTypes.size()+2];
        tableHeaders[0] = "序号";
        tableHeaders[1] = "姓名";
        for (int i = 0; i < performanceTypes.size(); i++) {
            tableHeaders[i+2] = performanceTypes.get(i).getName();
        }
        Sheet sheet = hssWb.createSheet("代表履职统计");
        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
        Integer[] total = new Integer[performanceTypes.size()];
        Row row = sheet.createRow(beginIndex);
        for (MemberCountVo memberCountVo : vos) {
            // 编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // 代表姓名
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(memberCountVo.getName());

            for (int i = 0; i < memberCountVo.getCount().size(); i++) {
                // 履职类型
                Cell cell2 = row.createCell(i+2);
                cell2.setCellValue(memberCountVo.getCount().get(i).getCount());
                Integer number = total[i]==null?0:total[i];
                total[i] = number+memberCountVo.getCount().get(i).getCount();
            }
        }
        if (dto.getSize() == 9999){//导出全部，加一个总计
            // 编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(vos.size());

            // 代表姓名
            Cell cell1 = row.createCell(1);
            cell1.setCellValue("总计");

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
            LOGGER.error("导出履职统计失败 \n {}", e1);
        }
    }


    private Map<String, Map<String, Integer>> dealPerformance(List<Performance> performanceList) {
        Map<String, Map<String, Integer>> memberMaps = Maps.newHashMap();
        for (Performance performance : performanceList) {
            String memberUid = performance.getNpcMember().getUid();
            Map<String, Integer> countMap = memberMaps.getOrDefault(memberUid,Maps.newHashMap());
            countMap.put(performance.getPerformanceType().getUid(),countMap.getOrDefault(performance.getPerformanceType().getUid(),0));
            memberMaps.put(memberUid,countMap);
        }
        return memberMaps;
    }

    private List<Performance> getPerformanceList(MemberCountDto dto, UserDetailsImpl userDetails) {
        List<Performance> performanceList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //提出代表
            if (StringUtils.isNotEmpty(dto.getName())) {
                predicates.add(cb.like(root.get("npcMember").get("name").as(String.class), "%" + dto.getName() + "%"));
            }
            //履职时间 开始
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
            //查询与bgAdmin同级的代表
            predicateList.add(cb.equal(root.get("level"), userDetails.getLevel()));
            predicateList.add(cb.isFalse(root.get("isDel")));
            predicateList.add(cb.equal(root.get("status").as(Byte.class),StatusEnum.ENABLED.getValue()));
            predicateList.add(cb.equal(root.get("area").get("uid"), userDetails.getArea().getUid()));
            //同镇的代表 or 同区的代表
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("town").get("uid"), userDetails.getTown().getUid()));
            }
            //按姓名模糊查询
            if (StringUtils.isNotBlank(dto.getName())) {
                predicateList.add(cb.like(root.get("name"), "%" + dto.getName() + "%"));
            }
            //按工作单位查询
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
     * 交换类型的顺序
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
