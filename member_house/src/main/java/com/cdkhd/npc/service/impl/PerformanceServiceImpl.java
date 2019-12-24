package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Performance;
import com.cdkhd.npc.entity.PerformanceType;
import com.cdkhd.npc.entity.dto.PerformanceDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeAddDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeDto;
import com.cdkhd.npc.entity.vo.PerformanceTypeVo;
import com.cdkhd.npc.entity.vo.PerformanceVo;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.util.Constant;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    @Autowired
    public PerformanceServiceImpl(PerformanceRepository performanceRepository, PerformanceTypeRepository performanceTypeRepository) {
        this.performanceRepository = performanceRepository;
        this.performanceTypeRepository = performanceTypeRepository;
    }

    /**
     * 条件查询履职类型
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
            predicates.add(cb.equal(root.get("level").as(Byte.class),"userDetails.getLevel"));
            if (1==1){
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), "userDetalis.getTown"));
            }else if (2==2){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), "userDetalis.getArea"));
            }
            if (performanceTypeDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), performanceTypeDto.getStatus()));
            }
            //按名称查询
            if (StringUtils.isNotEmpty(performanceTypeDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%"+ performanceTypeDto.getName() +"%"));
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
     * @param userDetails
     * @param performanceTypeAddDto
     * @return
     */
    @Override
    public RespBody addOrUpdatePerformanceType(UserDetailsImpl userDetails, PerformanceTypeAddDto performanceTypeAddDto) {
        RespBody body = new RespBody();
        if (StringUtils.isNotEmpty(performanceTypeAddDto.getName())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("类型名称不能为空！");
            return body;
        }
        PerformanceType performanceType;
        //如果uid不为空，说明是修改操作
        if (StringUtils.isNotEmpty(performanceTypeAddDto.getUid())){
            performanceType = performanceTypeRepository.findByUid(performanceTypeAddDto.getUid());
            if (performanceType == null){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("履职类型信息有误！");
                return body;
            }
        }else{
            //uid为空说明是添加操作
            performanceType = new PerformanceType();
        }
        performanceType.setName(performanceTypeAddDto.getName());
        performanceType.setRemark(performanceTypeAddDto.getName());
//        performanceType.setLevel();
//        performanceType.setArea();
//        performanceType.setTown();
        performanceTypeRepository.saveAndFlush(performanceType);
        return body;
    }

    /**
     * 删除履职类型
     * @param uid
     * @return
     */
    @Override
    public RespBody deletePerformanceType(String uid) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到履职类型！");
            return body;
        }
        performanceType.setIsDel(true);
        performanceTypeRepository.saveAndFlush(performanceType);
        return body;
    }

    /**
     * 调整类型顺序
     * @param uid 类型uid
     * @param type 上移  下移
     * @return
     */
    @Override
    public RespBody changeTypeSequence(String uid, Byte type) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到履职类型！");
            return body;
        }
        PerformanceType targetType;
        if (type.equals(Constant.LOGIN_WAY_UP)) {//1 上移
                Sort sort= new Sort(Sort.Direction.DESC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            targetType = performanceTypeRepository.findBySequenceDesc(performanceType.getSequence(),page);
        }else{
            Sort sort= new Sort(Sort.Direction.ASC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            targetType = performanceTypeRepository.findBySequenceAsc(performanceType.getSequence(),page);
        }
        List<PerformanceType> types = this.changeSequence(performanceType,targetType);
        performanceTypeRepository.saveAll(types);
        return body;
    }

    /**
     * 修改履职类型状态
     * @param uid
     * @param status  1 开启 2 关闭
     * @return
     */
    @Override
    public RespBody changeTypeStatus(String uid, Byte status) {
        RespBody body = new RespBody();
        PerformanceType performanceType = performanceTypeRepository.findByUid(uid);
        if (performanceType == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到履职类型！");
            return body;
        }
        performanceType.setStatus(status);
        performanceTypeRepository.saveAndFlush(performanceType);
        return body;
    }

    @Override
    public RespBody findPerformance(UserDetailsImpl userDetails, PerformanceDto performanceDto) {
        RespBody body = new RespBody();
        //查询代表的履职之前首先
        int begin = performanceDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performanceDto.getSize(), Sort.Direction.fromString(performanceDto.getDirection()), performanceDto.getProperty());
        Page<Performance> orderPage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class),"userDetails.getLevel"));
            if (1==1){
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), "userDetalis.getTown"));
            }else if (2==2){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), "userDetalis.getArea"));
            }
            //标题
            if (StringUtils.isNotEmpty(performanceDto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%"+ performanceDto.getTitle() +"%"));
            }
            //类型
            if (StringUtils.isNotEmpty(performanceDto.getPerformanceType())) {
                predicates.add(cb.equal(root.get("performanceType").get("uid").as(String.class), performanceDto.getPerformanceType()));
            }
            //提出代表
            if (StringUtils.isNotEmpty(performanceDto.getNpcMember())){
                predicates.add(cb.like(root.get("npcMember").get("name").as(String.class), "%"+ performanceDto.getNpcMember() +"%"));
            }
            //履职时间 开始
            if (performanceDto.getWorkAtStart() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), performanceDto.getWorkAtStart()));
            }
            if (performanceDto.getWorkAtEnd() != null){
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), performanceDto.getWorkAtEnd()));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);

        PageVo<PerformanceVo> vo = new PageVo<>(orderPage, performanceDto);
        List<PerformanceVo> performances = orderPage.getContent().stream().map(PerformanceVo::convert).collect(Collectors.toList());
        vo.setContent(performances);
        body.setData(vo);

        return body;
    }

    /**
     * 交换类型的顺序
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
}
