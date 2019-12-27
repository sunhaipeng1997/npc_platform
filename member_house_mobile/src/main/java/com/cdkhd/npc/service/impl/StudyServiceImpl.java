package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.StudyTypeVo;
import com.cdkhd.npc.enums.Level;
import com.cdkhd.npc.enums.Status;
import com.cdkhd.npc.repository.member_house.*;
import com.cdkhd.npc.service.StudyService;
import com.cdkhd.npc.vo.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudyServiceImpl.class);

    private StudyTypeRepository studyTypeRepository;

    @Autowired
    public StudyServiceImpl(StudyTypeRepository studyTypeRepository) {
        this.studyTypeRepository = studyTypeRepository;
    }


    @Override
    public RespBody studiesList(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<StudyType> studyTypeList;
        if (userDetails.getLevel().equals(Level.TOWN.getValue())) {
            studyTypeList = studyTypeRepository.findByStatusAndLevelAndTownUidOrderBySequenceAsc(Status.ENABLED.getValue(),Level.TOWN.getValue(),userDetails.getTown().getUid());
        }else{
            studyTypeList = studyTypeRepository.findByStatusAndLevelAndAreaUidOrderBySequenceAsc(Status.ENABLED.getValue(),Level.TOWN.getValue(),userDetails.getArea().getUid());
        }
        List<StudyTypeVo> studyTypeVos = studyTypeList.stream()
                .map(StudyTypeVo::convert)
                .sorted(Comparator.comparing(StudyTypeVo::getSequence))
                .collect(Collectors.toList());
        body.setData(studyTypeVos);
        return body;
    }



}
