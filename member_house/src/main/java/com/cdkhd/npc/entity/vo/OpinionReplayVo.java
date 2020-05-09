package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.OpinionReply;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * @描述 意见管理后端分页查询
 */
@Setter
@Getter
public class OpinionReplayVo extends BaseVo {

    //内容
    private String content;

    public static OpinionReplayVo convert(OpinionReply opinionReply) {
        OpinionReplayVo vo = new OpinionReplayVo();
        BeanUtils.copyProperties(opinionReply, vo);
        vo.setContent(opinionReply.getReply());
        return vo;
    }
}
