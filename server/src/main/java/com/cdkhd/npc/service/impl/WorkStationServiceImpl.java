package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.WorkStation;
import com.cdkhd.npc.entity.dto.UploadPicDto;
import com.cdkhd.npc.entity.dto.WorkStationAddDto;
import com.cdkhd.npc.entity.dto.WorkStationPageDto;
import com.cdkhd.npc.entity.vo.WorkStationPageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.member_house.WorkStationRepository;
import com.cdkhd.npc.service.WorkStationService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
public class WorkStationServiceImpl implements WorkStationService {

    private final WorkStationRepository workStationRepository;

    @Autowired
    public WorkStationServiceImpl(WorkStationRepository workStationRepository) {
        this.workStationRepository = workStationRepository;
    }

    @Override
    public RespBody page(UserDetailsImpl userDetails, WorkStationPageDto workStationPageDto) {
        RespBody body = new RespBody();
        int begin = workStationPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, workStationPageDto.getSize(), Sort.Direction.fromString(workStationPageDto.getDirection()), workStationPageDto.getProperty());
        Page<WorkStation> pageRes = workStationRepository.findAll((Specification<WorkStation>)(root, query, cb) -> {
            Predicate predicate = root.isNotNull();
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){  //如果是镇上的管理员就查询镇上的工作站
                predicate = cb.and(predicate, cb.equal(root.get("town").get("name").as(String.class), userDetails.getTown().getName()));
            }
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){  //如果是县上的管理员就查询县上的工作站
                predicate = cb.and(predicate, cb.equal(root.get("area").get("name").as(String.class), userDetails.getArea().getName()));
            }
            if (StringUtils.isNotBlank(workStationPageDto.getSearchKey())){
                predicate = cb.and(predicate, cb.like(root.get("name").as(String.class), "%" + workStationPageDto.getSearchKey() + "%"));
                predicate = cb.or(predicate, cb.like(root.get("address").as(String.class), "%" + workStationPageDto.getSearchKey() + "%"));
                predicate = cb.or(predicate, cb.like(root.get("telephone").as(String.class), "%" + workStationPageDto.getSearchKey() + "%"));
            }
            return predicate;
        }, page);
        PageVo<WorkStationPageVo> vo = new PageVo<>(pageRes, workStationPageDto);
        vo.setContent(pageRes.stream().map(WorkStationPageVo::convert).collect(Collectors.toList()));
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdate(UserDetailsImpl userDetails, WorkStationAddDto workStationAddDto) {
        RespBody body = new RespBody();
        WorkStation workStation = null;
        if (StringUtils.isBlank(workStationAddDto.getUid())){  //uid为空，说明是添加操作
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){  //区上工作站
                workStation = workStationRepository.findByAreaUidAndName(userDetails.getArea().getUid(), workStationAddDto.getName());
            }
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){  //镇上工作站
                workStation = workStationRepository.findByTownUidAndName(userDetails.getTown().getUid(), workStationAddDto.getName());
            }
            if (workStation != null){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("该名称已经存在");
                return body;
            }
            workStation = new WorkStation();
            workStationAddDto.setUid(workStation.getUid());
            BeanUtils.copyProperties(workStationAddDto, workStation);
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                workStation.setArea(userDetails.getArea());
            }else {
                workStation.setTown(userDetails.getTown());
            }
            workStation.setLevel(userDetails.getLevel());
        }else {  //uid不为空，说明是修改操作
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){  //区上工作站
                workStation = workStationRepository.findByAreaUidAndNameAndUidIsNot(userDetails.getArea().getUid(), workStationAddDto.getName(), workStationAddDto.getUid());
            }
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){  //镇上工作站
                workStation = workStationRepository.findByTownUidAndNameAndUidIsNot(userDetails.getTown().getUid(), workStationAddDto.getName(), workStationAddDto.getUid());
            }
            if (workStation != null){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("该名称已经存在");
                return body;
            }
            workStation = workStationRepository.findByUid(workStationAddDto.getUid());
            if (workStation == null) {
                body.setMessage("找不到该工作站");
                body.setStatus(HttpStatus.NOT_FOUND);
                return body;
            }
            BeanUtils.copyProperties(workStationAddDto, workStation);
        }
        workStationRepository.saveAndFlush(workStation);
        return body;
    }

    @Override
    public RespBody upload(UploadPicDto uploadPicDto) {
        RespBody<JSONObject> body = new RespBody();
        String uid = uploadPicDto.getUid();
        if (StringUtils.isBlank(uid)){
            body.setMessage("uid 不能为空");

        }
        return body;
    }

    @Override
    public RespBody changeStatus(String uid) {
        RespBody body = new RespBody();
        WorkStation workStation = workStationRepository.findByUid(uid);
        if (workStation == null){
            body.setMessage("找不到该工作站");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        workStation.setEnabled(!workStation.getEnabled());
        workStationRepository.saveAndFlush(workStation);
        return body;
    }

    @Override
    public RespBody delete(String uid) {
        RespBody body = new RespBody();
        WorkStation workStation = workStationRepository.findByUid(uid);
        if (workStation != null){
            workStationRepository.delete(workStation);
        }
        return body;
    }
}
