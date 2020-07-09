package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.UnitImage;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;
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

    //附议人
    private List<String> secondNames;

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
    private Boolean view = false;

    //代表是否查看办完的建议
    private Boolean doneView = false;

    //审核过后代表是否查看
    private Boolean npcView = true;

    //代表是否查看附议办结的建议
    private Boolean secondDoneView = false;

    //transUid
    private String transUid;

    //建议类型信息
    private SugBusVo sugBusVo;

    //提出代表信息
    private NpcMemberVo npcMemberVo;

    //单位延期记录
    private List<DelaySuggestionVo> delaySuggestionVos;

    //评价详情
    private AppraiseVo appraiseVo;

    //办理结果详情
    private ResultVo resultVo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date raiseTime;

    public static SugDetailVo convert(Suggestion suggestion) {
        SugDetailVo vo = new SugDetailVo();
        // 拷贝一些基本属性
        BeanUtils.copyProperties(suggestion, vo);
        vo.setSugBusVo(SugBusVo.convert(suggestion.getSuggestionBusiness()));
        //提出人
        NpcMember npcMember = suggestion.getRaiser();

        List<SuggestionImage> imageList = new ArrayList<>(suggestion.getSuggestionImages());
        imageList.sort(Comparator.comparing(SuggestionImage::getCreateTime));
        vo.setImages(imageList.stream().map(SuggestionImage::getUrl).collect(Collectors.toList()));

        if (npcMember != null) {
            vo.setNpcMemberVo(NpcMemberVo.convert(npcMember));
        }
        vo.setStatusName(SuggestionStatusEnum.getName(suggestion.getStatus()));

        vo.setSecondNames(suggestion.getSecondedSet().stream().map(second -> second.getNpcMember().getName()).collect(Collectors.toList()));

        //评价
        if (Objects.nonNull(suggestion.getAppraise())) {
            vo.setAppraiseVo(AppraiseVo.convert(suggestion.getAppraise()));
        }

        //结果
        if (Objects.nonNull(suggestion.getResult())) {
            vo.setResultVo(ResultVo.convert(suggestion.getResult()));
        }

        return vo;
    }
}
