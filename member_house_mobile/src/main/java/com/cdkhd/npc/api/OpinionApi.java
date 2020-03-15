package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.service.OpinionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/opinion")
public class OpinionApi {

    private OpinionService opinionService;

    @Autowired
    public OpinionApi(OpinionService opinionService) {
        this.opinionService = opinionService;
    }

    /**
     * 添加意见
     *
     * @param userDetails
     * @return
     */
    @PostMapping("/addOpinion")
    public ResponseEntity addOpinion(@CurrentUser UserDetailsImpl userDetails, AddOpinionDto addOpinionDto) {
        RespBody body = opinionService.addOpinion(userDetails, addOpinionDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 我的意见列表
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/myOpinions")
    public ResponseEntity myOpinions(@CurrentUser UserDetailsImpl userDetails, OpinionDto opinionDto) {
        RespBody body = opinionService.myOpinions(userDetails, opinionDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 意见详情
     *
     * @return
     */
    @GetMapping("/detailOpinion")
    public ResponseEntity detailOpinion(OpinionDetailDto opinionDetailDto) {
        RespBody body = opinionService.detailOpinion(opinionDetailDto);
        return ResponseEntity.ok(body);
    }

    //代表意见

    /**
     * 我收到的意见列表
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/receiveOpinions")
    public ResponseEntity receiveOpinions(@CurrentUser UserDetailsImpl userDetails, OpinionDto opinionDto) {
        RespBody body = opinionService.receiveOpinions(userDetails, opinionDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 回复意见
     *
     * @return
     */
    @PostMapping("/replyOpinion")
    public ResponseEntity replyOpinion(OpinionReplyDto opinionReplyDto) {
        RespBody body = opinionService.replyOpinion(opinionReplyDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 回复意见
     * @return
     */
    @PostMapping("/memberRecOpins")
    public ResponseEntity memberRecOpins(UidDto uidDto) {
        RespBody body = opinionService.memberRecOpins(uidDto);
        return ResponseEntity.ok(body);
    }

}
