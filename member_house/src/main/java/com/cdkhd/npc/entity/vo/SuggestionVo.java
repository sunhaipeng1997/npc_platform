package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author  ly
 * @Date 2020-01-07
 */

@Setter
@Getter
public class SuggestionVo extends BaseVo {

    //建议内容
	private String content;

   	//审核原因
	private String reason;

   	//状态
	private Byte status;

	//状态名称
	private String statusName;

   	//标题
	private String title;

   	//类型
    private SuggestionBusinessVo suggestionBusiness;

    //业务类型名称
    private String businessName;

   	//审核时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date auditAt;

    //代表信息
    private String memberName;

    //代表手机号
    private String memberMobile;

    //审核人
    private String auditor;

    public static SuggestionVo convert(Suggestion suggestion) {
        SuggestionVo vo = new SuggestionVo();
        BeanUtils.copyProperties(suggestion, vo);
        vo.setMemberName(suggestion.getRaiser().getName());
        vo.setMemberMobile(suggestion.getRaiser().getMobile());
        if (suggestion.getAuditor() != null) {
            vo.setAuditor(suggestion.getAuditor().getName());
        }
        else {
            vo.setAuditor("未审核");
        }
        vo.setSuggestionBusiness(SuggestionBusinessVo.convert(suggestion.getSuggestionBusiness()));
        vo.setBusinessName(suggestion.getSuggestionBusiness().getName());
        return vo;
    }
}
