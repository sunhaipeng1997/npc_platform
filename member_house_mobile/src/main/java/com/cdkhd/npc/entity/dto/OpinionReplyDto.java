package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class OpinionReplyDto extends BaseVo {

    //回复内容
    private String content;

}
