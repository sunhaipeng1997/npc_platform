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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SuggestionServiceImpl implements SuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceServiceImpl.class);

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
     * 建议业务类型下拉列表
     */
    @Override
    public RespBody sugBusList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> sb = Lists.newArrayList();
        //区上和街道，都查询区上的类型
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))){
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(), userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
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
        if (town != null && town.getType().equals(LevelEnum.AREA.getValue())){//如果是街道，那么查询街道的履职类型
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), town.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (StringUtils.isNotEmpty(townUid)){//镇的话
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(), townUid, StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = suggestionBusinesses.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    /**
     * 条件查询建议类型
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
            //按名称查询
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
     * 添加或修改建议业务类型
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
            body.setMessage("类型名称不能为空！");
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
            body.setMessage("类型名称已经存在！");
            return body;
        }
        //如果uid不为空，说明是修改操作
        if (StringUtils.isNotEmpty(suggestionBusinessAddDto.getUid())) {
            suggestionBusiness = suggestionBusinessRepository.findByUid(suggestionBusinessAddDto.getUid());
            if (suggestionBusiness == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("业务类型信息有误！");
                return body;
            }
        } else {
            //uid为空说明是添加操作
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
     * 删除业务类型
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
            body.setMessage("找不到业务类型！");
            return body;
        }
        suggestionBusiness.setIsDel(true);
        suggestionBusinessRepository.saveAndFlush(suggestionBusiness);
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
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(uid);
        if (suggestionBusiness == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到建议类型！");
            return body;
        }
        SuggestionBusiness target = null;
        if (type.equals(Constant.LOGIN_WAY_UP)) {//1 上移
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
            body.setMessage("移动失败！");
            return body;
        }
//        if (!type.equals(Constant.LOGIN_WAY_UP)) {//1 上移
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
     * 修改业务类型状态
     *
     * @param uid
     * @param status 1 开启 2 关闭
     * @return
     */
    @Override
    public RespBody changeBusinessStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(uid);
        if (suggestionBusiness == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到业务类型！");
            return body;
        }
        suggestionBusiness.setStatus(status);
        suggestionBusinessRepository.saveAndFlush(suggestionBusiness);
        return body;
    }

    /**
     * 获取已提建议信息列表
     *
     * @return
     */
    @Override
    public RespBody findSuggestion(UserDetailsImpl userDetails, SuggestionDto suggestionDto) {
        RespBody body = new RespBody();
        //查询代表的建议之前首先查询系统配置
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
     * 导出建议信息
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

        String fileName = ExcelCode.encodeFileName("代表已提建议信息.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //暴露Content-Disposition响应头，以便前端可以获取文件名
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        String[] tableHeaders = new String[]{"编号", "建议类型", "建议标题", "审核时间", "提出代表", "建议内容", "所属地区", "联系方式", "审核人", "建议状态", "审核意见","建议所在行政等级"};

        Sheet sheet = hssWb.createSheet("代表建议");

        Row headRow = sheet.createRow(0);
        int colSize = tableHeaders.length;
        for (int i = 0; i < colSize; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
        }

        int beginIndex = 1;
        for (Suggestion suggestion : suggestions) {
            Row row = sheet.createRow(beginIndex);

            // 编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // 建议类型
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(suggestion.getSuggestionBusiness().getName());

            // 建议类型
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(suggestion.getTitle());

            // 建议时间
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(suggestion.getAuditTime());

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

//            // 代表联系方式
//            Cell cell7 = row.createCell(7);
//            cell7.setCellValue(suggestion.getRaiser().getMobile());

            //审核人姓名
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(suggestion.getAuditor()!= null ? suggestion.getAuditor().getName():"");

            //状态
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(SuggestionStatusEnum.getName(suggestion.getStatus()));

            //审核意见
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(suggestion.getReason());

            //审核意见
            Cell cell11 = row.createCell(11);
            cell11.setCellValue(LevelEnum.getName(suggestion.getLevel()));

        }
        try {
            hssWb.write(os);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("导出代表建议出错 \n {}", e1);
        }
    }

    @Override
    public RespBody memberSuggestionCount(MemberCountDto dto, UserDetailsImpl userDetails) {
        RespBody<PageVo<MemberCountVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        Page<NpcMember> pageRes = this.getNpcMemberPage(dto,userDetails,page);//分页获取所有代表
        PageVo<MemberCountVo> vo = new PageVo<>(pageRes, dto);//代表分页对象
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,page,pageRes);
        vo.setContent(vos);
        body.setData(vo);
        return body;
    }

    private List<MemberCountVo> getMemberCountVos(MemberCountDto dto, UserDetailsImpl userDetails, Pageable page, Page<NpcMember> pageRes){
        List<NpcMember> content = pageRes.getContent();//代表列表
        List<MemberCountVo> vos = Lists.newArrayList();
        List<SuggestionBusiness> suggestionBusinesses = Lists.newArrayList();//获取所有可用的建议类型
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue())){
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(),userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }
        List<Suggestion> suggestionList = this.getSuggestionList(dto,userDetails);//获取所有履职信息
        Map<String, Map<String,Integer>> memberSuggestionMap = this.dealSuggestion(suggestionList);//处理所有履职信息
        for (NpcMember npcMember : content) {//遍历代表信息
            MemberCountVo memberCountVo = new MemberCountVo();
            memberCountVo.setUid(npcMember.getUid());
            memberCountVo.setName(npcMember.getName());
            List<CountVo> countList = Lists.newArrayList();
            Map<String,Integer> countMap = memberSuggestionMap.getOrDefault(npcMember.getUid(), Maps.newHashMap());
            for (SuggestionBusiness suggestionBusiness : suggestionBusinesses) {//遍历所有铝箔纸类型信息
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
     * 导出建议信息
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
        Page<NpcMember> pageRes = this.getNpcMemberPage(dto,userDetails,page);//分页获取所有代表
        List<MemberCountVo> vos = this.getMemberCountVos(dto,userDetails,page, pageRes);
        String fileName = ExcelCode.encodeFileName("代表建议统计.xls", req);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf-8''" + fileName);
        //暴露Content-Disposition响应头，以便前端可以获取文件名
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        List<SuggestionBusiness> suggestionBusinesses = Lists.newArrayList();//获取所有可用的建议类型
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            suggestionBusinesses = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(),userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
        }

        String[] tableHeaders = new String[suggestionBusinesses.size()+2];
        tableHeaders[0] = "序号";
        tableHeaders[1] = "姓名";
        for (int i = 0; i < suggestionBusinesses.size(); i++) {
            tableHeaders[i+2] = suggestionBusinesses.get(i).getName();
        }
        Sheet sheet = hssWb.createSheet("代表建议统计");
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

            // 编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(beginIndex);
            beginIndex++;

            // 代表姓名
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(memberCountVo.getName());

            for (int i = 0; i < memberCountVo.getCount().size(); i++) {
                // 建议类型
                Cell cell2 = row.createCell(i+2);
                cell2.setCellValue(memberCountVo.getCount().get(i).getCount());
                Integer number = total[i]==null?0:total[i];
                total[i] = number+memberCountVo.getCount().get(i).getCount();
            }
        }
        if (dto.getSize() == 9999){//导出全部，加一个总计
            Row row = sheet.createRow(beginIndex);
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
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("导出建议统计出错 \n {}", e1);
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
            //提出代表
            if (StringUtils.isNotEmpty(dto.getName())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + dto.getName() + "%"));
            }
            //提出建议时间 开始
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
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));//如果是镇上的，就只能查询镇上的
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//区后台管理员的查询
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (!suggestionDto.isFlag()){//查询下级乡镇的，那么level只能是镇和街道
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                }else {//否则，就是查询区上的
                    SystemSetting systemSetting = this.getSystemSetting(userDetails);//判断开关是否打开
                    if (systemSetting.getShowSubPerformance()) {//下级履职开关打开
                        //先把所有区代表查询出来
                        List<NpcMember> areaMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),LevelEnum.AREA.getValue());
                        List<NpcMember> allMembers = Lists.newArrayList();//区代表所有的身份，包括区代表身份和区代表在镇上的代表身份
                        for (NpcMember areaMember : areaMembers) {
                            if (areaMember.getAccount()!=null) {//注冊了的代表
                                allMembers.addAll(areaMember.getAccount().getNpcMembers());
                            }else{//未注册的代表
                                allMembers.add(areaMember);
                            }
                        }
                        List<String> memberUid = Lists.newArrayList();
                        for (NpcMember member : allMembers) {
                            memberUid.add(member.getUid());//本次需要查询的所有代表的uid
                        }
                        if (CollectionUtils.isNotEmpty(memberUid)) {
                            predicates.add(cb.in(root.get("raiser").get("uid")).value(memberUid));
                        }
                    }else{//开关没有打开，就只查询区代表值区上的建议情况
                        predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
                    }
                }
            }
            //标题
            if (StringUtils.isNotEmpty(suggestionDto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + suggestionDto.getTitle() + "%"));
            }
            //下属镇
            if (!suggestionDto.isFlag() && userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                if ( StringUtils.isNotEmpty(suggestionDto.getTownUid()) ){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), suggestionDto.getTownUid()));
                }
            }
            //类型
            if (StringUtils.isNotEmpty(suggestionDto.getSuggestionBusiness())) {
                predicates.add(cb.equal(root.get("suggestionBusiness").get("uid").as(String.class), suggestionDto.getSuggestionBusiness()));
            }
            //提出代表
            if (StringUtils.isNotEmpty(suggestionDto.getName())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + suggestionDto.getName() + "%"));
            }
            if (StringUtils.isNotEmpty(suggestionDto.getMobile())) {
                predicates.add(cb.equal(root.get("raiser").get("mobile").as(String.class), suggestionDto.getMobile()));
            }
            //审核时间 开始
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
