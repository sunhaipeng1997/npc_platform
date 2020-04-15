package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Study;
import com.cdkhd.npc.entity.StudyType;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.StudyTypeVo;
import com.cdkhd.npc.entity.vo.StudyVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.member_house.StudyRepository;
import com.cdkhd.npc.repository.member_house.StudyTypeRepository;
import com.cdkhd.npc.service.StudyService;
import com.cdkhd.npc.util.Constant;
import com.cdkhd.npc.util.SysUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudyServiceImpl.class);


    private StudyTypeRepository studyTypeRepository;

    private StudyRepository studyRepository;

    @Autowired
    public StudyServiceImpl(StudyTypeRepository studyTypeRepository, StudyRepository studyRepository) {
        this.studyTypeRepository = studyTypeRepository;
        this.studyRepository = studyRepository;
    }

    @Override
    public RespBody findStudyType(UserDetailsImpl userDetails, StudyTypeDto studyTypeDto) {
        RespBody body = new RespBody();
        int begin = studyTypeDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, studyTypeDto.getSize(), Sort.Direction.fromString(studyTypeDto.getDirection()), studyTypeDto.getProperty());

        Page<StudyType> studyTypePage = studyTypeRepository.findAll((Specification<StudyType>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            if (studyTypeDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), studyTypeDto.getStatus()));
            }
            //按名称查询
            if (StringUtils.isNotEmpty(studyTypeDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + studyTypeDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);

        PageVo<StudyTypeVo> vo = new PageVo<>(studyTypePage, studyTypeDto);
        List<StudyTypeVo> studyTypeVos = studyTypePage.getContent().stream().map(StudyTypeVo::convert).collect(Collectors.toList());
        vo.setContent(studyTypeVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdateStudyType(UserDetailsImpl userDetails, StudyTypeAddDto studyTypeAddDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(studyTypeAddDto.getName())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("类型名称不能为空！");
            LOGGER.error("添加修改类型时类型名称不能为空！");
            return body;
        }
        StudyType studyType = null;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
            if (StringUtils.isEmpty(studyTypeAddDto.getUid())) {
                studyType = studyTypeRepository.findByNameAndLevelAndTownUidAndIsDelFalse(studyTypeAddDto.getName(), userDetails.getLevel(), userDetails.getTown().getUid());
            }else{
                studyType = studyTypeRepository.findByNameAndLevelAndTownUidAndIsDelFalseAndUidNot(studyTypeAddDto.getName(), userDetails.getLevel(), userDetails.getTown().getUid(),studyTypeAddDto.getUid());
            }
        }if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            if (StringUtils.isEmpty(studyTypeAddDto.getUid())) {
                studyType = studyTypeRepository.findByNameAndLevelAndAreaUidAndIsDelFalse(studyTypeAddDto.getName(), userDetails.getLevel(), userDetails.getArea().getUid());
            }else{
                studyType = studyTypeRepository.findByNameAndLevelAndAreaUidAndIsDelFalseAndUidNot(studyTypeAddDto.getName(), userDetails.getLevel(), userDetails.getArea().getUid(),studyTypeAddDto.getUid());
            }
        }
        if (studyType != null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("类型名称已经存在！");
            LOGGER.error("类型名称已经存在！");
            return body;
        }
        //如果uid不为空，说明是修改操作
        if (StringUtils.isNotEmpty(studyTypeAddDto.getUid())) {
            studyType = studyTypeRepository.findByUid(studyTypeAddDto.getUid());
            if (studyType == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("学习类型信息有误！");
                LOGGER.error("\n {} 的学习类型查询不出来！",studyTypeAddDto.getUid());
                return body;
            }
        } else {
            //uid为空说明是添加操作
            studyType = new StudyType();
            studyType.setLevel(userDetails.getLevel());
            studyType.setArea(userDetails.getArea());
            studyType.setTown(userDetails.getTown());
            Integer maxSequence;
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
                maxSequence = studyTypeRepository.findMaxSequenceByLevelAndTownUid(userDetails.getLevel(), userDetails.getTown().getUid());
            }else{
                maxSequence = studyTypeRepository.findMaxSequenceByLevelAndAreaUid(userDetails.getLevel(), userDetails.getArea().getUid());
            }
            if(maxSequence == null){//防治数据库初始为空时报错，所以将初始序号设置为0。（李亚林）
                maxSequence = 0;
            }
            studyType.setSequence(maxSequence + 1);
        }
        studyType.setName(studyTypeAddDto.getName());
        studyType.setRemark(studyTypeAddDto.getRemark());

        studyTypeRepository.saveAndFlush(studyType);
        return body;
    }

    @Override
    public RespBody deleteStudyType(String uid) {
        RespBody body = new RespBody();
        StudyType studyType = studyTypeRepository.findByUid(uid);
        if (studyType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到学习类型！");
            return body;
        }
        studyType.setIsDel(true);
        studyTypeRepository.saveAndFlush(studyType);
        return body;
    }

    @Override
    public RespBody changeTypeSequence(UserDetailsImpl userDetails,String uid, Byte type) {
        RespBody body = new RespBody();
        StudyType studyType = studyTypeRepository.findByUid(uid);
        if (studyType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到学习类型！");
            return body;
        }
        StudyType targetType;
        if (type.equals(Constant.LOGIN_WAY_UP)) {//1 上移
            Sort sort = new Sort(Sort.Direction.DESC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                targetType = studyTypeRepository.findByTownSequenceDesc(studyType.getSequence(), userDetails.getLevel(), userDetails.getTown().getUid(), page).getContent().get(0);
            }else{
                targetType = studyTypeRepository.findByAreaSequenceDesc(studyType.getSequence(), userDetails.getLevel(), userDetails.getArea().getUid(), page).getContent().get(0);
            }
        } else {
            Sort sort = new Sort(Sort.Direction.ASC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                targetType = studyTypeRepository.findByTownSequenceAsc(studyType.getSequence(), userDetails.getLevel(), userDetails.getTown().getUid(), page).getContent().get(0);
            }else{
                targetType = studyTypeRepository.findByAreaSequenceAsc(studyType.getSequence(), userDetails.getLevel(), userDetails.getArea().getUid(), page).getContent().get(0);
            }

        }
        List<StudyType> types = this.changeSequence(studyType, targetType);
        studyTypeRepository.saveAll(types);
        return body;
    }

    @Override
    public RespBody changeTypeStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        StudyType studyType = studyTypeRepository.findByUid(uid);
        if (studyType == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到学习类型！");
            return body;
        }
        studyType.setStatus(status);
        studyTypeRepository.saveAndFlush(studyType);
        return body;
    }

    @Override
    public RespBody studyTypeList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<StudyType> studyTypeList = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            studyTypeList = studyTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalse(userDetails.getLevel(),userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            studyTypeList = studyTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalse(userDetails.getLevel(),userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
        }
        studyTypeList.sort(Comparator.comparing(StudyType::getSequence));
        List<CommonVo> commonVos = studyTypeList.stream().map(type -> CommonVo.convert(type.getUid(),type.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody findStudy(UserDetailsImpl userDetails, StudyDto studyDto) {
        RespBody body = new RespBody();
        //查询代表的履职之前首先查询系统配置
        int begin = studyDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, studyDto.getSize(), Sort.Direction.fromString(studyDto.getDirection()), studyDto.getProperty());
        Page<Study> studyPage = this.getStudies(userDetails,studyDto, page);
        PageVo<StudyVo> vo = new PageVo<>(studyPage, studyDto);
        List<StudyVo> performances = studyPage.getContent().stream().map(StudyVo::convert).collect(Collectors.toList());
        vo.setContent(performances);
        body.setData(vo);
        return body;
    }

    private Page<Study> getStudies(UserDetailsImpl userDetails, StudyDto studyDto, Pageable page) {
        Page<Study> studyPage = studyRepository.findAll((Specification<Study>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("studyType").get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            predicates.add(cb.isFalse(root.get("studyType").get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //标题
            if (StringUtils.isNotEmpty(studyDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + studyDto.getName() + "%"));
            }
            //类型
            if (StringUtils.isNotEmpty(studyDto.getStudyType())) {
                predicates.add(cb.equal(root.get("studyType").get("uid").as(String.class), studyDto.getStudyType()));
            }
            //上传时间 开始
            if (studyDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), studyDto.getDateStart()));
            }
            if (studyDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), studyDto.getDateEnd()));
            }
            if (studyDto.getStatus() != null){
                predicates.add(cb.equal(root.get("status").as(Byte.class), studyDto.getStatus()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        return studyPage;
    }

    @Override
    public RespBody deleteStudy(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到学习资料！");
            return body;
        }
        Study study = studyRepository.findByUid(uid);
        if (study == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到学习资料！");
            return body;
        }
        studyRepository.delete(study);
        return body;
    }

    /**
     * 添加或修改学习资料
     * @param userDetails
     * @param studyAddDto
     * @return
     */
    @Override
    public RespBody addOrUpdateStudy(UserDetailsImpl userDetails, StudyAddDto studyAddDto) {
        RespBody body = new RespBody();
        Study study;
        if(StringUtils.isEmpty(studyAddDto.getName())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("学习资料名称不能为空！");
            return body;
        }
        if(StringUtils.isEmpty(studyAddDto.getStudyType())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("学习类型不能为空！");
            return body;
        }
        if(StringUtils.isEmpty(studyAddDto.getUrl())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("学习资料文件不能为空！");
            return body;
        }
        if (StringUtils.isEmpty(studyAddDto.getUid())) {//添加学习资料
            study = new Study();
            study.setLevel(userDetails.getLevel());
            study.setArea(userDetails.getArea());
            study.setTown(userDetails.getTown());
            Integer maxSequence = studyRepository.findMaxSequence(studyAddDto.getStudyType());
            study.setSequence(maxSequence==null?1:maxSequence+1);
        }
        else {
            study = studyRepository.findByUid(studyAddDto.getUid());
            if (study == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("找不到学习资料！");
                return body;
            }
        }
        study.setName(studyAddDto.getName());
        study.setStudyType(studyTypeRepository.findByUid(studyAddDto.getStudyType()));
        study.setRemark(studyAddDto.getRemark());
        study.setUrl(studyAddDto.getUrl());
        studyRepository.saveAndFlush(study);
        return body;
    }

    /**
     * 上传学习文件
     * @param userDetails
     * @param file
     * @return
     */
    @Override
    public RespBody uploadStudyFile(UserDetailsImpl userDetails, MultipartFile file) {
        RespBody<JSONObject> body = new RespBody<>();
        if (file == null) {
            body.setMessage("文件不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        // 保存新文件
        String org = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(org);
        // 生成新的文件名
        String filename = String.format("%s.%S", SysUtil.uid(), ext);
        String parentPath = "static/public/study";
        File bgFile = new File(parentPath, filename);
        File parentFile = bgFile.getParentFile();
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            if (!mkdirs) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("系统内部错误");
                return body;
            }
        }

        try (InputStream is = file.getInputStream()) {
            // 拷贝文件
            FileUtils.copyInputStreamToFile(is, bgFile);
            JSONObject obj = new JSONObject();
            obj.put("url", String.format("/public/study/%s", filename));
            obj.put("name", org);
            body.setData(obj);
        } catch (IOException e) {
            LOGGER.error("文件保存失败 {}", e);
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("系统内部错误");
            return body;
        }
        return body;
    }

    /**
     * 修改学习资料的排序号
     * @param uid
     * @param type
     * @return
     */
    @Override
    public RespBody changeStudySequence(String uid, Byte type, String studyType) {
        RespBody body = new RespBody();
        Study study = studyRepository.findByUid(uid);
        if (study == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到学习资料！");
            return body;
        }
        if (StringUtils.isEmpty(studyType)){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("只能调整同一类型下的学习资料！");
            return body;
        }
        Study targetStudy;
        if (type.equals(Constant.LOGIN_WAY_UP)) {//1 上移
            Sort sort = new Sort(Sort.Direction.DESC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            targetStudy = studyRepository.findBySequenceDesc(study.getSequence(), studyType, page).getContent().get(0);
        } else {
            Sort sort = new Sort(Sort.Direction.ASC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            targetStudy = studyRepository.findBySequenceAsc(study.getSequence(), studyType, page).getContent().get(0);
    }
        List<Study> types = this.changeSequence(study, targetStudy);
        studyRepository.saveAll(types);
        return body;
    }

    /**
     * 修改学习资料状态
     * @param uid
     * @param status
     * @return
     */
    @Override
    public RespBody changeStudyStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        Study study = studyRepository.findByUid(uid);
        if (study == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到学习资料！");
            return body;
        }
        study.setStatus(status);
        studyRepository.saveAndFlush(study);
        return body;
    }

    /**
     * 小程序学习资料列表展示
     * @return
     */
    @Override
    public RespBody studyList(LevelDto levelDto) {
        RespBody body = new RespBody();
        List<StudyType> studyTypeList;
        if (levelDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            studyTypeList = studyTypeRepository.findByStatusAndLevelAndTownUidOrderBySequenceAsc(StatusEnum.ENABLED.getValue(), LevelEnum.TOWN.getValue(),levelDto.getUid());
        }else{
            studyTypeList = studyTypeRepository.findByStatusAndLevelAndAreaUidOrderBySequenceAsc(StatusEnum.ENABLED.getValue(), LevelEnum.AREA.getValue(),levelDto.getUid());
        }
        List<StudyTypeVo> studyTypeVos = studyTypeList.stream()
                .filter(type -> !type.getIsDel() && type.getStatus().equals(StatusEnum.ENABLED.getValue()))
                .map(StudyTypeVo::convert)
                .sorted(Comparator.comparing(StudyTypeVo::getSequence))
                .collect(Collectors.toList());
        body.setData(studyTypeVos);
        return body;
    }

    /**
     * 交换类型的顺序
     *
     * @param studyType
     * @param targetType
     * @return
     */
    private List<StudyType> changeSequence(StudyType studyType, StudyType targetType) {
        List<StudyType> typeList = Lists.newArrayList();
        Integer beforeSec = studyType.getSequence();
        studyType.setSequence(targetType.getSequence());
        targetType.setSequence(beforeSec);
        typeList.add(studyType);
        typeList.add(targetType);
        return typeList;
    }

    /**
     * 交换学习资料的顺序
     *
     * @param study
     * @param targetStudy
     * @return
     */
    private List<Study> changeSequence(Study study, Study targetStudy) {
        List<Study> studyList = Lists.newArrayList();
        Integer beforeSec = study.getSequence();
        study.setSequence(targetStudy.getSequence());
        targetStudy.setSequence(beforeSec);
        studyList.add(study);
        studyList.add(targetStudy);
        return studyList;
    }
}
