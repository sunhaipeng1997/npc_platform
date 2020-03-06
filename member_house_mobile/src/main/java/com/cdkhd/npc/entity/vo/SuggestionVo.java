package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionReply;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @创建人
 * @创建时间 2019/12/24
 * @描述
 */
@Getter
@Setter
public class SuggestionVo extends BaseVo {

    //建议标题
    private String title;

    //建议的内容
    private String content;

    //提出代表信息
    private NpcMemberVo npcMemberVo;

    private int my_suggestion_number;

    private int timeout;

    public static SuggestionVo convert(Suggestion suggestion) {
        SuggestionVo vo = new SuggestionVo();

        // 拷贝一些基本属性
        BeanUtils.copyProperties(suggestion, vo);

        //提出人
        NpcMember npcMember = suggestion.getRaiser();
        if (npcMember != null){
            vo.setNpcMemberVo(NpcMemberVo.convert(npcMember));
        }

        Set<SuggestionReply> replies = suggestion.getReplies();
        if (replies != null && !replies.isEmpty()){
            AtomicInteger vieww = new AtomicInteger();
            replies.stream().map(reply -> {
                long view = reply.getView();
                if (view == 0){
                    vieww.getAndIncrement();
                }
                return vieww;
            }).collect(Collectors.toSet());
            vo.setMy_suggestion_number(vieww.get());
        }

        Integer timeout = 2;
        //fixme 这个地方小程序返回的时间有点问题
        Date expireAt = DateUtils.addMinutes(suggestion.getCreateTime(), timeout);
        int view = suggestion.getView();
        if (expireAt.before(new Date()) || view == 1){
            vo.setTimeout(0);
        }else {
            vo.setTimeout(1);
        }
        return vo;
    }
}
