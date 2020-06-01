package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.SuggestionReply;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @创建人 LiYang
 * @创建时间 2020/05/18
 * @描述 查看建议详情vo
 */
@Getter
@Setter
public class SugDetailVo extends BaseVo {

    //建议标题
    private String title;

    //建议的内容
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date raiseTime;

    //状态
    private Byte status;

    //是否可撤回
    private Boolean canOperate;

    //建议状态名称
    private String statusName;

    //审核原因
    private String auditReason;

    //图片路径
    private List<String> images;

    //审核人员是否查看待审核信息
    private Boolean view;

    //代表是否查看审核结果
    private Boolean myView = true;

    //代表是否查看办完的建议
    private Boolean doneView;

    //transUid
    private String transUid;

    //建议类型信息
    private SugBusVo sugBusVo;

    //提出代表信息
    private NpcMemberVo npcMemberVo;

    private int my_suggestion_number;

    private int timeout;

    public static SugDetailVo convert(Suggestion suggestion) {
        SugDetailVo vo = new SugDetailVo();
        // 拷贝一些基本属性
        BeanUtils.copyProperties(suggestion, vo);
        vo.setSugBusVo(SugBusVo.convert(suggestion.getSuggestionBusiness()));
        //提出人
        NpcMember npcMember = suggestion.getRaiser();
        vo.setImages(suggestion.getSuggestionImages().stream().map(SuggestionImage::getUrl).collect(Collectors.toList()));
        if (npcMember != null) {
            vo.setNpcMemberVo(NpcMemberVo.convert(npcMember));
        }
        Set<SuggestionReply> replies = suggestion.getReplies();
        if (replies != null) {
            AtomicInteger vieww = new AtomicInteger();
            replies.stream().map(reply -> {
                boolean view = reply.getView();
                if (!view) {
                    vieww.getAndIncrement();
                    vo.setMyView(false);
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
        if (expireAt.before(new Date()) || view) {
            vo.setTimeout(0);
        } else {
            vo.setTimeout(1);
        }
        return vo;
    }
}
