package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.dto.SuggestionBusinessAddDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessDto;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.entity.vo.SuggestionBusinessVo;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.NpcSuggestionService;
import com.cdkhd.npc.util.Constant;
import com.cdkhd.npc.util.ExcelCode;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NpcSuggestionServiceImpl implements NpcSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcSuggestionServiceImpl.class);

    private final SuggestionBusinessRepository suggestionBusinessRepository;

    private final SuggestionRepository suggestionRepository;

    private final SystemSettingRepository systemSettingRepository;

    private final NpcMemberRepository npcMemberRepository;

    @Autowired
    public NpcSuggestionServiceImpl(SuggestionBusinessRepository suggestionBusinessRepository, SuggestionRepository suggestionRepository, SystemSettingRepository systemSettingRepository, NpcMemberRepository npcMemberRepository) {
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionRepository = suggestionRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.npcMemberRepository = npcMemberRepository;
    }

    @Override
    public RespBody sugBusList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> sb = Lists.newArrayList();
        //区上和街道，都查询区上的类型
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) || (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))){
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(userDetails.getLevel(), userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

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
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                Set<SuggestionBusiness> suggestionBusinesses = suggestionBusinessRepository.findByTownUid(userDetails.getTown().getUid());
                maxSequence = suggestionBusiness != null ? suggestionBusinesses.size() + 1 : 1;
            }
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
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
        List<SuggestionBusiness> sb = this.changeSequence(suggestionBusiness, target);
        suggestionBusinessRepository.saveAll(sb);
        return body;
    }

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

        String[] tableHeaders = new String[]{"编号", "建议类型", "建议标题", "提出时间", "提出代表", "联系方式", "建议内容", "所属地区",  "建议状态", "审核人",  "审核意见", "审核日期", "建议办理单位"};

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
                Cell cell5 = row.createCell(5);
                cell4.setCellValue(member.getName());
                String mobile = member.getMobile();
                if (StringUtils.isNotBlank(mobile)) {
                    cell5.setCellValue(mobile);
                } else {
                    cell5.setCellValue("");
                }
            } else {
                cell2.setCellValue("");
            }

            //建议内容
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(suggestion.getContent());

            //所属地区
            Cell cell7 = row.createCell(7);
            String place = "";
            if (suggestion.getLevel().equals(LevelEnum.AREA.getValue())) {
                place = suggestion.getArea().getName() + suggestion.getTown().getName();
            } else if (suggestion.getLevel().equals(LevelEnum.TOWN.getValue())) {
                place = suggestion.getTown().getName();  // + suggestion.getNpcMemberGroup().getName();
            }
            cell7.setCellValue(place);

            //状态
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(SuggestionStatusEnum.getName(suggestion.getStatus()));

            //审核人姓名
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(suggestion.getAuditor() != null ? suggestion.getAuditor().getName() : "");

            //审核意见
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(suggestion.getReason());

            //审核日期
            Cell cell11 = row.createCell(11);
            cell11.setCellValue(simpleDateFormat.format(suggestion.getAuditTime()));

            //建议办理单位
            Cell cell12 = row.createCell(12);
            cell12.setCellValue(suggestion.getUnit() == null ? "" : suggestion.getUnit().getName());

        }
        try {
            hssWb.write(os);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.error("导出代表建议出错 \n {}", e1);
        }
    }

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

            Predicate predicate = cb.greaterThan(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue());
            predicates.add(predicate);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));//如果是镇上的，就只能查询镇上的
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//区后台管理员的查询
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                if (!suggestionDto.isFlag()) {//查询下级乡镇的，那么level只能是镇和街道
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                } else {//否则，就是查询区上的
                    SystemSetting systemSetting = this.getSystemSetting(userDetails);//判断开关是否打开
                    if (systemSetting.getShowSubPerformance()) {//下级履职开关打开
                        //先把所有区代表查询出来
                        List<NpcMember> areaMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
                        List<NpcMember> allMembers = Lists.newArrayList();//区代表所有的身份，包括区代表身份和区代表在镇上的代表身份
                        for (NpcMember areaMember : areaMembers) {
                            if (areaMember.getAccount() != null) {//注冊了的代表
                                allMembers.addAll(areaMember.getAccount().getNpcMembers());
                            } else {//未注册的代表
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
                    } else {//开关没有打开，就只查询区代表值区上的建议情况
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
                if (StringUtils.isNotEmpty(suggestionDto.getTownUid())) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), suggestionDto.getTownUid()));
                }
            }
            if (suggestionDto.getStatus() != null){
                predicates.add(cb.equal(root.get("status").as(Byte.class), suggestionDto.getStatus()));
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
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndTownUid(userDetails.getLevel(), userDetails.getTown().getUid());
        } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(userDetails.getLevel(), userDetails.getArea().getUid());
        }
        return systemSetting;
    }
}
