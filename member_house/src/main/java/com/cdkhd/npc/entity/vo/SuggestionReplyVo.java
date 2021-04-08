package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionReply;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * @Description
 * @Author  ly
 * @Date 2020-01-07
 */

@Setter
@Getter
public class SuggestionReplyVo extends BaseVo {

    //回复内容
    private String reply;

    //代表查看回复状态
    private Boolean view = false;

    //回复的代表
    private String replyer;

    public static SuggestionReplyVo convert(SuggestionReply suggestionReply) {
        SuggestionReplyVo vo = new SuggestionReplyVo();
        vo.setReply(suggestionReply.getReply());
        vo.setCreateTime(suggestionReply.getCreateTime());
        vo.setView(suggestionReply.getView());
        vo.setReplyer(suggestionReply.getReplyer().getName());
        return vo;
    }
}
