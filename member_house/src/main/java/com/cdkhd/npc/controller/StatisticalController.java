package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.OpinionPageDto;
import com.cdkhd.npc.service.OpinionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/member_house/statistic")
public class StatisticalController {

    private OpinionService opinionService;

    @Autowired
    public StatisticalController(OpinionService opinionService) {
        this.opinionService = opinionService;
    }


    /**
     * 获取意见列表
     * @return
     */
    @GetMapping("/opinionPage")
    public ResponseEntity opinionPage(@CurrentUser UserDetailsImpl userDetails, OpinionPageDto opinionPageDto) {
        RespBody body = opinionService.opinionPage(userDetails,opinionPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 导出意见
     * @return
     */
    @PostMapping("/exportOpinion")
    public void exportOpinion(@CurrentUser UserDetailsImpl userDetails, OpinionPageDto opinionPageDto, HttpServletRequest req, HttpServletResponse res) {
        opinionService.exportOpinion(userDetails,opinionPageDto,req,res);
    }

}
