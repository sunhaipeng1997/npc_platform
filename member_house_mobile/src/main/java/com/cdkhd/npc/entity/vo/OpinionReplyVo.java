package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.BaseDomain;
import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.entity.OpinionReply;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class OpinionReplyVo extends BaseDomain {


    //回复内容
	private String reply;

    //是否查看
	private Long view;

    public static OpinionReplyVo convert(OpinionReply opinionReply) {
        OpinionReplyVo vo = new OpinionReplyVo();
        BeanUtils.copyProperties(opinionReply, vo);
        return vo;
    }

}
