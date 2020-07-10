package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.persistence.Column;

/**
 * @创建人
 * @创建时间 2020/05/18
 * @描述
 */
@Getter
@Setter
public class SugBusVo extends BaseVo {

    //类型名称
    @Column(name = "name" )
    private String name;

    //排序号
    @Column(name = "sequence" )
    private Integer sequence;

    public static SugBusVo convert(SuggestionBusiness suggestionBusiness) {
        SugBusVo vo = new SugBusVo();
        BeanUtils.copyProperties(suggestionBusiness, vo);
        return vo;
    }
}
