package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.entity.vo.NpcMemberVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NpcMemberService;
import com.cdkhd.npc.service.SessionService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NpcMemberServiceImpl implements NpcMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberServiceImpl.class);

    private NpcMemberRepository npcMemberRepository;

    @Autowired
    public NpcMemberServiceImpl(NpcMemberRepository npcMemberRepository) {
        this.npcMemberRepository = npcMemberRepository;
    }



    /**
     * 分页查询代表信息
     * @param userDetails 当前用户
     * @param level 查询条件
     * @param uid 查询条件
     * @return 查询结果
     */
    @Override
    public RespBody allNpcMembers(UserDetailsImpl userDetails, Byte level, String uid) {

        RespBody body = new RespBody();
        //其它查询条件
        Specification<NpcMember> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //查询与bgAdmin同级的代表
            predicateList.add(cb.equal(root.get("level"), level));
            predicateList.add(cb.isFalse(root.get("isDel")));
            //同镇的代表 or 同区的代表
            if (level.equals(LevelEnum.TOWN.getValue())) {
                predicateList.add(cb.equal(root.get("town").get("uid"), uid));
            } else if (level.equals(LevelEnum.AREA.getValue())) {
                predicateList.add(cb.equal(root.get("area").get("uid"), uid));
            }
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        List<NpcMember> npcMembers = npcMemberRepository.findAll(spec);
        //返回数据
        List<NpcMemberVo> npcMemberVos = npcMembers.stream().map(NpcMemberVo::convert).collect(Collectors.toList());
        body.setData(npcMemberVos);
        return body;
    }

    @Override
    public RespBody npcMemberUnits(UserDetailsImpl userDetails, Byte level) {
        return null;
    }

}
