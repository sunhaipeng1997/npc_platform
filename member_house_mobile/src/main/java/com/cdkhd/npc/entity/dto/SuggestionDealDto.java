package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @创建人 ly
 * @创建时间 2020/01/02
 * @描述
 */
@Setter
@Getter
public class SuggestionDealDto extends PageDto {

    //建议uid
    private String uid;

    //状态 1，草稿 2，提交审核
    private Byte status;

    //是否接受
    private Boolean accept;

    //原因
    private String reason;

    //拒绝原因
    private String refuseReason;

    //预计延期时间
    private Date applyDate;

    //时间延期时间
    private Date delayDate;

    //评价办理结果
    private Byte result;

    //评价办理态度
    private Byte attitude;
}
