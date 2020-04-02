package com.cdkhd.npc.api;


import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.LevelDto;
import com.cdkhd.npc.entity.dto.NewsPageDto;
import com.cdkhd.npc.entity.dto.NewsReviewDto;
import com.cdkhd.npc.service.NewsService;
import com.cdkhd.npc.service.StudyService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/mobile/study")
public class StudyApi {
    private StudyService studyService;

    @Autowired
    public StudyApi(StudyService studyService) {
        this.studyService = studyService;
    }

    /**
     *  小程序获取学习列表
     * @return
     */
    @GetMapping("/studyList")
    public ResponseEntity studyList(@CurrentUser UserDetailsImpl userDetails, LevelDto levelDto) {
        RespBody body = studyService.studyList(userDetails,levelDto);
        return ResponseEntity.ok(body);
    }


}
