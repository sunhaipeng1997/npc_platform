package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.StudyDto;
import com.cdkhd.npc.entity.dto.StudyTypeAddDto;
import com.cdkhd.npc.entity.dto.StudyTypeDto;
import com.cdkhd.npc.vo.RespBody;

public interface StudyService {

    //学习类型相关接口

    /**
     * 条件查询学习类型
     * @param userDetails
     * @param studyTypeDto
     * @return
     */
    RespBody findStudyType(UserDetailsImpl userDetails, StudyTypeDto studyTypeDto);

    /**
     * 添加、修改学习类型
     * @param userDetails
     * @param studyTypeAddDto
     * @return
     */
    RespBody addOrUpdateStudyType(UserDetailsImpl userDetails, StudyTypeAddDto studyTypeAddDto);

    /**
     * 删除学习类型
     * @param uid
     * @return
     */
    RespBody deleteStudyType(String uid);

    /**
     * 修改类型排序
     * @param uid
     * @param type
     * @return
     */
    RespBody changeTypeSequence(String uid, Byte type);

    /**
     * 修改类型状态
     * @param uid
     * @param status
     * @return
     */
    RespBody changeTypeStatus(String uid, Byte status);

    /**
     * 学习类型下拉
     * @param userDetails
     * @return
     */
    RespBody studyTypeList(UserDetailsImpl userDetails);


    //学习相关接口

    /**
     * 条件查询学习信息
     * @param userDetails
     * @param studyDto
     * @return
     */
    RespBody findStudy(UserDetailsImpl userDetails, StudyDto studyDto);


    /**
     * 删除学习信息
     * @param uid
     * @return
     */
    RespBody deleteStudy(String uid);


}
