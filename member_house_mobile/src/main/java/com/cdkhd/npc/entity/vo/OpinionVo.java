package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.entity.OpinionReply;
import com.cdkhd.npc.enums.ReplayStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class OpinionVo extends BaseVo {

    //意见内容
	private String content;

    //接受代表
    private String receiver;

    //图片
    private List<String> image;

    //提出人
    private String  sender;

    //提出人所在村
    private String address;

    //提出人手机号
    private String mobile;

    //是否回复
    private Byte status;
    private String statusName;

    //回复列表
    private List<OpinionReplyVo> replyVos;

    public static OpinionVo convert(Opinion opinion) {
        OpinionVo vo = new OpinionVo();
        BeanUtils.copyProperties(opinion, vo);
        vo.setStatusName(ReplayStatusEnum.getName(opinion.getStatus()));
        // 需要特殊处理的属性
        Account account = opinion.getSender();
        if (account != null){
            //发送者信息
            vo.setSender(account.getVoter().getRealname());
            vo.setAddress(account.getVoter().getVillage().getName());
            vo.setMobile(account.getVoter().getMobile());
        }

        NpcMember npcMember = opinion.getReceiver();
        if (npcMember != null){
            //接受代表信息
            vo.setReceiver(npcMember.getName());
        }
        //回复信息
        Set<OpinionReply> replies = opinion.getReplies();
        List<OpinionReplyVo> replyVos = replies.stream().map(OpinionReplyVo::convert).collect(Collectors.toList());
        vo.setReplyVos(replyVos);
        return vo;
    }
}
