package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.web.multipart.MultipartFile;

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
    RespBody changeTypeSequence(UserDetailsImpl userDetails,String uid, Byte type);

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


    /**
     * 添加/修改学习信息
     * @param userDetails
     * @param studyAddDto
     * @return
     */
    RespBody addOrUpdateStudy(UserDetailsImpl userDetails, StudyAddDto studyAddDto);

    /**
     * 上传学习资料
     * @param userDetails
     * @param file
     * @return
     */
    RespBody uploadStudyFile(UserDetailsImpl userDetails, MultipartFile file);

    /**
     * 修改学习资料排序
     * @param uid
     * @param type
     * @return
     */
    RespBody changeStudySequence(String uid, Byte type, String studyType);

    /**
     * 修改学习资料状态
     * @param uid
     * @param status
     * @return
     */
    RespBody changeStudyStatus(String uid, Byte status);


    //小程序相关接口

    /**
     * 学习资料列表展示
     * @param userDetails
     * @return
     */
    RespBody studyList(LevelDto levelDto);

}
