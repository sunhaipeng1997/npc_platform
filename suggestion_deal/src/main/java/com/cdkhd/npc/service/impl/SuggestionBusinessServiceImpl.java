package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.entity.dto.SuggestionBusinessAddDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessDto;
import com.cdkhd.npc.entity.vo.SuggestionBusinessVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.SuggestionBusinessService;
import com.cdkhd.npc.util.Constant;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.compress.utils.Lists;
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

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SuggestionBusinessServiceImpl implements SuggestionBusinessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestionBusinessServiceImpl.class);

    private SuggestionBusinessRepository suggestionBusinessRepository;

    private TownRepository townRepository;

    @Autowired
    public SuggestionBusinessServiceImpl(SuggestionBusinessRepository suggestionBusinessRepository,  TownRepository townRepository) {
        this.suggestionBusinessRepository = suggestionBusinessRepository;
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

}
