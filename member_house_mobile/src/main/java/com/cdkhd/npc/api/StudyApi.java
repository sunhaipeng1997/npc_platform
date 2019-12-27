package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.service.StudyService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/study")
public class StudyApi {

    private StudyService studyService;

    @Autowired
    public StudyApi(StudyService studyService) {
        this.studyService = studyService;
    }


    /**
     * 获取履职类型列表
     * @param userDetails
     * @return
     */
    @GetMapping("/studiesList")
    public ResponseEntity studiesList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = studyService.studiesList(userDetails);
        return ResponseEntity.ok(body);
    }


}
