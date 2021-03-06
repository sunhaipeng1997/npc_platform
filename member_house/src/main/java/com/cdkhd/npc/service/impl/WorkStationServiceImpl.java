package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.WorkStation;
import com.cdkhd.npc.entity.dto.WorkStationAddDto;
import com.cdkhd.npc.entity.dto.WorkStationPageDto;
import com.cdkhd.npc.entity.vo.WorkStationPageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.member_house.WorkStationRepository;
import com.cdkhd.npc.service.WorkStationService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkStationServiceImpl implements WorkStationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkStationServiceImpl.class);

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
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){  //??????????????????????????????????????????????????????
                predicate = cb.and(predicate, cb.equal(root.get("town").get("name").as(String.class), userDetails.getTown().getName()));
            }
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){  //??????????????????????????????????????????????????????
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
        if (StringUtils.isBlank(workStationAddDto.getUid())){  //uid??????????????????????????????
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){  //???????????????
                workStation = workStationRepository.findByAreaUidAndName(userDetails.getArea().getUid(), workStationAddDto.getName());
            }
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){  //???????????????
                workStation = workStationRepository.findByTownUidAndName(userDetails.getTown().getUid(), workStationAddDto.getName());
            }
            if (workStation != null){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("?????????????????????");
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
        }else {  //uid?????????????????????????????????
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){  //???????????????
                workStation = workStationRepository.findByAreaUidAndNameAndUidIsNot(userDetails.getArea().getUid(), workStationAddDto.getName(), workStationAddDto.getUid());
            }
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){  //???????????????
                workStation = workStationRepository.findByTownUidAndNameAndUidIsNot(userDetails.getTown().getUid(), workStationAddDto.getName(), workStationAddDto.getUid());
            }
            if (workStation != null){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("?????????????????????");
                return body;
            }
            workStation = workStationRepository.findByUid(workStationAddDto.getUid());
            if (workStation == null) {
                body.setMessage("?????????????????????");
                body.setStatus(HttpStatus.NOT_FOUND);
                return body;
            }
            BeanUtils.copyProperties(workStationAddDto, workStation);
        }
        workStationRepository.saveAndFlush(workStation);
        return body;
    }

    /**
     * ??????????????????????????????????????????
     * @param userDetails ??????????????????
     * @param avatar ????????????
     * @return ?????????????????????????????????????????????url
     */
    @Override
    public RespBody upload(UserDetailsImpl userDetails, MultipartFile avatar) {
        RespBody<String> body = new RespBody<>();
        if (avatar == null){
            body.setMessage("????????????????????????????????????");
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error("????????????????????????");
            return body;
        }
        //?????????????????????????????????
        String url = ImageUploadUtil.saveImage("work_station_avatar", avatar,200,200);
        if (url.equals("error")) {
            body.setMessage("????????????????????????????????????");
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error("????????????????????????");
            return body;
        }
        body.setMessage("??????????????????");
        body.setData(url);
        return body;
    }

//    @Override
//    public RespBody upload(UploadPicDto uploadPicDto) {
//        RespBody<JSONObject> body = new RespBody();
//        String uid = uploadPicDto.getUid();
//        if (StringUtils.isBlank(uid)){
//            body.setMessage("uid ????????????");
//
//        }
//        return body;
//    }

    @Override
    public RespBody changeStatus(String uid) {
        RespBody body = new RespBody();
        WorkStation workStation = workStationRepository.findByUid(uid);
        if (workStation == null){
            body.setMessage("?????????????????????");
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
