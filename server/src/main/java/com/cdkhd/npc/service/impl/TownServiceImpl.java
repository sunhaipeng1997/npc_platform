package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.entity.dto.TownAddDto;
import com.cdkhd.npc.entity.dto.TownPageDto;
import com.cdkhd.npc.entity.vo.TownDetailsVo;
import com.cdkhd.npc.entity.vo.TownPageVo;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.service.TownService;
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

import javax.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
public class TownServiceImpl implements TownService {

    //private final NpcMemberGroupRepository npcMemberGroupRepository;

    private final TownRepository townRepository;

//    private final AreaRepository areaRepository;
//
//    private final VillageRepository villageRepository;


    @Autowired
    public TownServiceImpl(TownRepository townRepository) {
        this.townRepository = townRepository;
    }
//    public TownServiceImpl(NpcMemberGroupRepository npcMemberGroupRepository, TownRepository townRepository, AreaRepository areaRepository, VillageRepository villageRepository) {
//        this.npcMemberGroupRepository = npcMemberGroupRepository;
//        this.townRepository = townRepository;
//        this.areaRepository = areaRepository;
//        this.villageRepository = villageRepository;
//    }

    @Override
    public RespBody page(UserDetailsImpl userDetails, TownPageDto townPageDto) {
        RespBody body = new RespBody();
        int begin = townPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, townPageDto.getSize(), Sort.Direction.fromString(townPageDto.getDirection()), townPageDto.getProperty());
        Page<Town> pageRes = townRepository.findAll((Specification<Town>)(root, query, cb) -> {
            Predicate predicate = root.isNotNull();
            predicate = cb.and(predicate, cb.equal(root.get("area").get("uid"), userDetails.getArea().getUid()));
            if (StringUtils.isNotEmpty(townPageDto.getSearchKey())){
                predicate = cb.and(predicate, cb.like(root.get("name").as(String.class), "%" + townPageDto.getSearchKey() + "%"));
            }
            return predicate;
        }, page);
        PageVo<TownPageVo> vo = new PageVo<>(pageRes, townPageDto);
        vo.setContent(pageRes.stream().map(TownPageVo::convert).collect(Collectors.toList()));
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody details(String uid) {
        RespBody body = new RespBody();
        Town town = townRepository.findByUid(uid);
        if (null == town){
            body.setMessage("找不到该镇");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        body.setData(TownDetailsVo.convert(town));
        return body;
    }

    @Override
    public RespBody add(UserDetailsImpl userDetails, TownAddDto townAddDto) {
        //添加镇需要创建镇管理员账号
        RespBody body = new RespBody();
        Town town = townRepository.findByAreaUidAndName(userDetails.getArea().getUid(), townAddDto.getName());
        if (null != town){
            body.setMessage("该镇已存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        town = townAddDto.convert();
        Area area = userDetails.getArea();
        town.setArea(area);
        townRepository.saveAndFlush(town);  //保存该镇
        return body;
    }

    @Override
    public RespBody update(UserDetailsImpl userDetails, TownAddDto townAddDto) {
        RespBody body = new RespBody();
        Town town = townRepository.findByUid(townAddDto.getUid());
        if (town == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该镇");
            return body;
        }
        town.setName(townAddDto.getName());
        town.setDescription(townAddDto.getDescription());
        townRepository.saveAndFlush(town);
        return body;
    }

    @Override
    public RespBody delete(String uid) {
        RespBody body = new RespBody();
        Town town = townRepository.findByUid(uid);
        if (town == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该镇");
            return body;
        }
        if (town.getNpcMemberGroups().size() > 0 || town.getVillages().size() > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前镇还包含代表小组/村的信息不能删除");
            return body;
        }
        townRepository.delete(town);
        return body;
    }
}
