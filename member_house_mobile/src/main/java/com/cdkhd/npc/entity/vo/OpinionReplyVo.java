package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.BaseDomain;
import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.entity.OpinionReply;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class OpinionReplyVo extends BaseVo {

    //回复内容
	private String reply;

    //是否查看
	private Long view;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "GMT+8")
	private Date replyDate;

    public static OpinionReplyVo convert(OpinionReply opinionReply) {
        OpinionReplyVo vo = new OpinionReplyVo();
        BeanUtils.copyProperties(opinionReply, vo);
        vo.setReplyDate(opinionReply.getCreateTime());
        return vo;
    }

}
