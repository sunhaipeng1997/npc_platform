package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.dto.GovSuggestionPageDto;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.UnitSuggestionService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UnitSuggestionServiceImpl implements UnitSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitSuggestionServiceImpl.class);

    private SuggestionRepository suggestionRepository;

    private GeneralService generalService;

    @Autowired
    public UnitSuggestionServiceImpl(SuggestionRepository suggestionRepository, GeneralService generalService) {
        this.suggestionRepository = suggestionRepository;
        this.generalService = generalService;
    }

    /**
     * 查询待办建议
     * @param userDetails
     * @param dto
     * @return
     */
    @Override
    public RespBody findToDeal(UserDetailsImpl userDetails, GovSuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();

        //按条件分页查询代办建议
        Page<Suggestion> page = this.findPage(userDetails, dto, SuggestionStatusEnum.TRANSFERRED_UNIT);

        //封装分页结果
        PageVo<SuggestionVo> pageVo = new PageVo<>(page, dto);
        pageVo.setContent(page.get().map(SuggestionVo::convert).collect(Collectors.toList()));

        body.setData(pageVo);
        return body;
    }

    /**
     * 分页查询建议
     * @param userDetails 当前用户
     * @param dto 查询参数
     * @param status 建议状态
     * @return 分页查询结果
     */
    private Page<Suggestion> findPage(UserDetailsImpl userDetails, GovSuggestionPageDto dto, SuggestionStatusEnum status) {
        //分页查询条件
        Pageable pageable = PageRequest.of(dto.getPage()-1, dto.getSize(),
                Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        //其他查询条件
        Specification<Suggestion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            /*predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("status").as(Byte.class), status.getValue()));

            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));//如果是镇上的，就只能查询镇上的
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {//区办理单位的查询
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
            }*/

            //标题
            if (StringUtils.isNotBlank(dto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + dto.getTitle() + "%"));
            }
            //类型
            if (StringUtils.isNotBlank(dto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestionBusiness").get("uid").as(String.class), dto.getBusiness()));
            }
            //提出代表
            if (StringUtils.isNotBlank(dto.getMember())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + dto.getMember() + "%"));
            }
            if (StringUtils.isNotBlank(dto.getMobile())) {
                predicates.add(cb.equal(root.get("raiser").get("mobile").as(String.class), dto.getMobile()));
            }
            //审核时间 开始
            if (dto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), dto.getDateStart()));
            }
            if (dto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), dto.getDateEnd()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        //基础查询条件
        Specification<Suggestion> baseSpec = generalService.basePredicates(userDetails, status);

        return suggestionRepository.findAll(spec.and(baseSpec), pageable);
    }
}
