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
public class OpinionListVo extends BaseVo {

    //意见内容
	private String content;

    //是否回复
    private Byte status;
    private String statusName;

    //接受人员是否查看
    private Boolean view;

    //超時
    private int timeout;

    //我是否查看回复
    private Boolean myView = true;


    public static OpinionListVo convert(Opinion opinion) {
        OpinionListVo vo = new OpinionListVo();
        BeanUtils.copyProperties(opinion, vo);
        vo.setStatusName(ReplayStatusEnum.getName(opinion.getStatus()));

        //超期撤回信息
        Integer timeout = 2;
        Date expireAt = DateUtils.addMinutes(opinion.getCreateTime(), timeout);
        if (expireAt.before(new Date()) || !opinion.getView()){
            vo.setTimeout(0);
        }else {
            vo.setTimeout(1);
        }
        for (OpinionReply reply : opinion.getReplies()) {
            if (!reply.getView()){
                vo.setMyView(false);
            }
        }
        return vo;
    }
}
