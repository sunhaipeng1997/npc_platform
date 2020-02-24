package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.StudyAddDto;
import com.cdkhd.npc.entity.dto.StudyDto;
import com.cdkhd.npc.entity.dto.StudyTypeAddDto;
import com.cdkhd.npc.entity.dto.StudyTypeDto;
import com.cdkhd.npc.service.StudyService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/server/study")
public class StudyController {

    private StudyService studyService;

    @Autowired
    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }


    /**
     * 获取学习类型列表
     * @return
     */
    @GetMapping("/studyType")
    public ResponseEntity studyType(@CurrentUser UserDetailsImpl userDetails, StudyTypeDto studyTypeDto) {
        RespBody body = studyService.findStudyType(userDetails,studyTypeDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 添加、修改学习类型
     * @return
     */
    @PostMapping("/addOrUpdateStudyType")
    public ResponseEntity addOrUpdateStudyType(@CurrentUser UserDetailsImpl userDetails, StudyTypeAddDto studyTypeAddDto) {
        RespBody body = studyService.addOrUpdateStudyType(userDetails,studyTypeAddDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除学习类型
     * @return
     */
    @DeleteMapping("/deleteStudyType")
    public ResponseEntity deleteStudyType(String uid) {
        RespBody body = studyService.deleteStudyType(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改类型排序
     * @return
     */
    @PostMapping("/changeTypeSequence")
    public ResponseEntity changeTypeSequence(@CurrentUser UserDetailsImpl userDetails,String uid, Byte type) {
        RespBody body = studyService.changeTypeSequence(userDetails, uid,type);
        return ResponseEntity.ok(body);
    }

    /**
     * 修改类型状态
     * @return
     */
    @PostMapping("/changeTypeStatus")
    public ResponseEntity changeTypeStatus(String uid, Byte status) {
        RespBody body = studyService.changeTypeStatus(uid,status);
        return ResponseEntity.ok(body);
    }

    /**
     * 类型下拉
     * @return
     */
    @GetMapping("/studyTypeList")
    public ResponseEntity studyTypeList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = studyService.studyTypeList(userDetails);
        return ResponseEntity.ok(body);
    }

    //学习相关接口


    /**
     * 获取学习信息列表
     * @return
     */
    @GetMapping("/findStudy")
    public ResponseEntity findStudy(@CurrentUser UserDetailsImpl userDetails, StudyDto studyDto) {
        RespBody body = studyService.findStudy(userDetails,studyDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除学习信息
     * @return
     */
    @DeleteMapping("/deleteStudy")
    public ResponseEntity deleteStudy(String uid) {
        RespBody body = studyService.deleteStudy(uid);
        return ResponseEntity.ok(body);
    }

    /**
     *  添加/修改学习信息
     * @return
     */
    @PostMapping("/addOrUpdateStudy")
    public ResponseEntity addOrUpdateStudy(@CurrentUser UserDetailsImpl userDetails,StudyAddDto studyAddDto) {
        RespBody body = studyService.addOrUpdateStudy(userDetails,studyAddDto);
        return ResponseEntity.ok(body);
    }


    /**
     *  上传学习资料
     * @return
     */
    @PostMapping("/uploadStudyFile")
    public ResponseEntity uploadStudyFile(@CurrentUser UserDetailsImpl userDetails, MultipartFile file) {
        RespBody body = studyService.uploadStudyFile(userDetails,file);
        return ResponseEntity.ok(body);
    }


    /**
     *  修改学习资料排序
     * @return
     */
    @PostMapping("/changeStudySequence")
    public ResponseEntity changeStudySequence(String uid, Byte type, String studyType) {
        RespBody body = studyService.changeStudySequence(uid,type, studyType);
        return ResponseEntity.ok(body);
    }


    /**
     *  修改学习资料状态
     * @return
     */
    @PostMapping("/changeStudyStatus")
    public ResponseEntity changeStudyStatus(String uid, Byte status) {
        RespBody body = studyService.changeStudyStatus(uid,status);
        return ResponseEntity.ok(body);
    }


}
