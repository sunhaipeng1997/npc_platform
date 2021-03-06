package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Village;
import com.cdkhd.npc.entity.dto.VillageAddDto;
import com.cdkhd.npc.entity.dto.VillagePageDto;
import com.cdkhd.npc.entity.vo.VillageVo;
import com.cdkhd.npc.enums.GroupEnum;
import com.cdkhd.npc.repository.base.NpcMemberGroupRepository;
import com.cdkhd.npc.repository.member_house.VillageRepository;
import com.cdkhd.npc.service.VillageService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VillageServiceImpl implements VillageService {

    private VillageRepository villageRepository;

    private NpcMemberGroupRepository npcMemberGroupRepository;

    @Autowired
    public VillageServiceImpl(VillageRepository villageRepository, NpcMemberGroupRepository npcMemberGroupRepository) {
        this.villageRepository = villageRepository;
        this.npcMemberGroupRepository = npcMemberGroupRepository;
    }

    @Override
    public RespBody findVillage(UserDetailsImpl userDetails, VillagePageDto villagePageDto) {
        RespBody body = new RespBody();
        int begin = villagePageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, villagePageDto.getSize(), Sort.Direction.fromString(villagePageDto.getDirection()), villagePageDto.getProperty());
        Page<Village> villagePage = villageRepository.findAll((Specification<Village>) (root, query, cb) -> {
            Predicate predicate = root.isNotNull();
            if (StringUtils.isNotEmpty(villagePageDto.getName())) {
                predicate = cb.and(predicate, cb.like(root.get("name").as(String.class), "%" + villagePageDto.getName() +"%"));
            }
            if (StringUtils.isNotEmpty(villagePageDto.getGroup())) {
                if (GroupEnum.UNGROUPED.getValue().equals(villagePageDto.getGroup())){
                    predicate = cb.and(predicate, cb.isNull(root.get("npcMemberGroup")));
                }else {
                    predicate = cb.and(predicate, cb.equal(root.get("npcMemberGroup").get("uid").as(String.class), villagePageDto.getGroup()));
                }
            }
            predicate = cb.and(predicate, cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));  //??????????????????????????????
            return predicate;
        }, page);
        PageVo<VillageVo> vo = new PageVo<>(villagePage, villagePageDto);
        List<VillageVo> villageVos = villagePage.getContent().stream().map(VillageVo::convert).collect(Collectors.toList());
        vo.setContent(villageVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdateVillage(UserDetailsImpl userDetails, VillageAddDto villageAddDto) {
        RespBody body = new RespBody();
        Village village;
        if (StringUtils.isEmpty(villageAddDto.getUid())) {  //?????????
            village = villageRepository.findByTownUidAndName(userDetails.getTown().getUid(), villageAddDto.getName());
            if (null != village){
                body.setMessage("???????????????");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }else {
                village = new Village();
                village.setTown(userDetails.getTown());
            }
        }else{  //?????????
            village = villageRepository.findByUid(villageAddDto.getUid());
            if (village == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("?????????????????????");
                return body;
            }
            Village village1 = villageRepository.findByTownUidAndNameAndUidIsNot(userDetails.getTown().getUid(), villageAddDto.getName(), village.getUid());
            if (village1 != null){
                body.setMessage("???????????????");
                body.setStatus(HttpStatus.BAD_REQUEST);
                return body;
            }
        }
        village.setName(villageAddDto.getName());
        village.setIntroduction(villageAddDto.getIntroduction());
        villageRepository.saveAndFlush(village);
        return body;
    }

    @Override
    public RespBody deleteVillage(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????");
            return body;
        }
        Village village = villageRepository.findByUid(uid);
        if (village == null) {
            body.setMessage("???????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (village.getVoters().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("??????????????????????????????????????????");
            return body;
        }
        villageRepository.delete(village);
        body.setMessage("????????????");
        return body;
    }

    @Override
    public RespBody optional(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<Village> villages = villageRepository.findByTownUidAndNpcMemberGroupIsNull(userDetails.getTown().getUid());
        List<VillageVo> villageVos = new ArrayList<>();
        for (Village village : villages) {
            villageVos.add(VillageVo.convert(village));
        }
        body.setData(villageVos);
        return body;
    }

    @Override
    public RespBody modifiable(UserDetailsImpl userDetails, String uid) {
        RespBody body = new RespBody();
        //?????????????????????
        List<Village> villages = new ArrayList<>(npcMemberGroupRepository.findByUid(uid).getVillages());
        //?????????????????????????????????
        List<Village> modifyGroupVillages = villageRepository.findAll((Specification<Village>) (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid());
            predicate = cb.and(predicate, cb.isNull(root.get("npcMemberGroup")));
            return predicate;
        });
        modifyGroupVillages.addAll(villages);
        modifyGroupVillages.sort((o1, o2) ->  Long.compare(o1.getId(), o2.getId()));
        List<List<VillageVo>> res = new ArrayList<>();
        res.add(villages.stream().map(VillageVo::convert).collect(Collectors.toList()));
        res.add(modifyGroupVillages.stream().map(VillageVo::convert).collect(Collectors.toList()));
        body.setData(res);
        return body;
    }
}
