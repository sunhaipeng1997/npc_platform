package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.enums.ReplayStatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @描述 意见管理后端分页查询
 */
@Setter
@Getter
public class OpinionVo extends PageDto {

    //接受代表名称
    private String memberName;

    //提出代表手机号
    private String mobile;

    //接受代表所属机构uid
    private String groupName;

    //内容
    private String content;

    //回复状态
    private Byte status;
    private String statusName;

    //提出人姓名
    private String senderName;

    //意见回复内容
    private List<OpinionReplayVo> replayVoList;

    //意见图片
    private List<String> images;

    public static OpinionVo convert(Opinion opinion) {
        OpinionVo vo = new OpinionVo();
        BeanUtils.copyProperties(opinion, vo);
        vo.setMemberName(opinion.getReceiver().getName());
        vo.setSenderName(opinion.getSender().getVoter().getRealname());
        vo.setMobile(opinion.getSender().getVoter().getMobile());
        vo.setStatusName(ReplayStatusEnum.getName(opinion.getStatus()));
        vo.setReplayVoList(opinion.getReplies().stream().map(OpinionReplayVo::convert).collect(Collectors.toList()));
        vo.setImages(opinion.getImages().stream().map(img -> img.getPicture()).collect(Collectors.toList()));
        return vo;
    }
}
