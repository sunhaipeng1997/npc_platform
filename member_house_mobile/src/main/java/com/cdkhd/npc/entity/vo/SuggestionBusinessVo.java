package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.SuggestionBusiness;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.persistence.Column;

/**
 * @创建人
 * @创建时间 2019/01/06
 * @描述
 */
@Getter
@Setter
public class SuggestionBusinessVo extends BaseVo {

    //类型名称
    @Column(name = "name" )
    private String name;

    //排序号
    @Column(name = "sequence" )
    private Integer sequence;

    public static SuggestionBusinessVo convert(SuggestionBusiness suggestionBusiness) {
        SuggestionBusinessVo vo = new SuggestionBusinessVo();
        BeanUtils.copyProperties(suggestionBusiness, vo);
        return vo;
    }
}
