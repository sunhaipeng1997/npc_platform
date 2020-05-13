package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.dto.SuggestionDto;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.GovSuggestionService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GovSuggestionServiceImpl implements GovSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovSuggestionServiceImpl.class);

    private SuggestionRepository suggestionRepository;

    @Autowired
    public GovSuggestionServiceImpl(SuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    public RespBody getGovSuggestion(UserDetailsImpl userDetails, SuggestionDto suggestionDto) {
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
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
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

}
