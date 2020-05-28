package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.ConveyStatusEnum;
import com.cdkhd.npc.enums.GovDealStatusEnum;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description
 * @Author  ly
 * @Date 2020-01-07
 */

@Setter
@Getter
public class ConveyProcessVo extends BaseVo {

    //转办结果 0 单位待处理  1、单位接受  2、单位拒绝
    private Byte status;
    private String statusName;

    //政府处理状态  0 未处理 1 已重新分配 2 无需重新分配
    private Byte dealStatus;
    private String dealStatusName;

    //本次转办是否处理完成  完成：单位接受了转办  或者  单位拒绝，政府已经重新转办或者选择不转办
    private Boolean dealDone;

    //办理单位处理时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date unitDealTime;

    //转办次数
    private Integer conveyTimes;

    //转办时间
    private Date conveyTime;

    //拒绝原因
    private String remark;

    //办理单位类型 1、主办单位  2、协办单位
    private Byte type;
    private String typeName;

    //办理单位是否已读，1：已读，0：未读
    private Boolean unitView;

    //办理单位回复是否已读，1：已读，0：未读
    private Boolean govView;

    //目标办理单位
    private UnitVo unitVo;

    //转办人
    private String conveyUser;

    //对应的建议
    private SuggestionVo suggestionVo;

    public static ConveyProcessVo convertSug(ConveyProcess conveyProcess) {
        ConveyProcessVo vo = new ConveyProcessVo();
        BeanUtils.copyProperties(conveyProcess,vo);
        vo.setStatusName(ConveyStatusEnum.getName(conveyProcess.getStatus()));
        vo.setDealStatusName(GovDealStatusEnum.getName(conveyProcess.getDealStatus()));
        vo.setTypeName(UnitTypeEnum.getName(conveyProcess.getType()));
        vo.setUnitVo(UnitVo.convert(conveyProcess.getUnit()));
        vo.setSuggestionVo(SuggestionVo.convert(conveyProcess.getSuggestion()));
        return vo;
    }


    public static ConveyProcessVo convert(ConveyProcess conveyProcess) {
        ConveyProcessVo vo = new ConveyProcessVo();
        BeanUtils.copyProperties(conveyProcess,vo);
        vo.setStatusName(ConveyStatusEnum.getName(conveyProcess.getStatus()));
        vo.setDealStatusName(GovDealStatusEnum.getName(conveyProcess.getDealStatus()));
        vo.setTypeName(UnitTypeEnum.getName(conveyProcess.getType()));
        vo.setUnitVo(UnitVo.convert(conveyProcess.getUnit()));
        return vo;
    }
}
