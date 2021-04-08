package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * @Description
 * @Author  ly
 * @Date 2019-01-07
 */
@Getter
@Setter
public class SuggestionBusinessVo extends BaseVo {

    //类型名称
	private String name;

    //类型状态
    private Byte status;

    //类型状态名称
    private String statusName;

    //排序
    private Integer sequence;

    //说明
    private String remark;

    public static SuggestionBusinessVo convert(SuggestionBusiness suggestionBusiness) {
        SuggestionBusinessVo vo = new SuggestionBusinessVo();
        BeanUtils.copyProperties(suggestionBusiness, vo);
        vo.setStatusName(StatusEnum.getName(suggestionBusiness.getStatus()));
        return vo;
    }
}
