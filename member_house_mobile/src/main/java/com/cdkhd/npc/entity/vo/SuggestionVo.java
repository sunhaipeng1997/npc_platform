package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.SuggestionReply;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
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

    //状态
    private Byte status;
    private String statusName;

    //审核原因
    private String auditReason;

    private List<String> images;

    private Boolean view;

    public static SuggestionVo convert(Suggestion suggestion) {
        SuggestionVo vo = new SuggestionVo();
        // 拷贝一些基本属性
        BeanUtils.copyProperties(suggestion, vo);
        //提出人
        NpcMember npcMember = suggestion.getRaiser();
        vo.setImages(suggestion.getSuggestionImages().stream().map(SuggestionImage::getUrl).collect(Collectors.toList()));
        if (npcMember != null){
            vo.setNpcMemberVo(NpcMemberVo.convert(npcMember));
        }
        Set<SuggestionReply> replies = suggestion.getReplies();
        if (replies != null && !replies.isEmpty()){
            AtomicInteger vieww = new AtomicInteger();
            replies.stream().map(reply -> {
                boolean view = reply.getView();
                if (view == false){
                    vieww.getAndIncrement();
                }
                return vieww;
            }).collect(Collectors.toSet());
            vo.setMy_suggestion_number(vieww.get());
        }
        vo.setStatusName(SuggestionStatusEnum.getName(suggestion.getStatus()));

        Integer timeout = 2;
        Date expireAt = DateUtils.addMinutes(suggestion.getCreateTime(), timeout);
        Boolean view = suggestion.getView();
        vo.setView(view);
        if (expireAt.before(new Date()) || view){
            vo.setTimeout(0);
        }else {
            vo.setTimeout(1);
        }
        return vo;
    }
}
