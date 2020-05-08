package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Performance;
import com.cdkhd.npc.entity.PerformanceImage;
import com.cdkhd.npc.enums.PerformanceStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class PerformanceVo extends BaseVo {

    //履职内容
	private String content;

   	//审核原因
	private String reason;

   	//状态
	private Byte status;
	private String statusName;

   	//标题
	private String title;

   	//类型
    private String performanceType;
    private String typeName;

   	//履职时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date workAt;

    //代表信息
    private String memberName;

    //代表手机号
    private String memberMobile;

    //审核人
    private String auditor;

    private List<String> images;

    //审核人是否查看
    private Boolean view;

    //我是否查看审核结果
    private Boolean myView;

    private String transUid;

    public static PerformanceVo convert(Performance performance) {
        PerformanceVo vo = new PerformanceVo();
        BeanUtils.copyProperties(performance, vo);
        vo.setMemberName(performance.getNpcMember().getName());
        vo.setMemberMobile(performance.getNpcMember().getMobile());
        vo.setStatusName(PerformanceStatusEnum.getName(performance.getStatus()));
        if (performance.getAuditor() != null) {
            vo.setAuditor(performance.getAuditor().getName());
        }else{
            vo.setAuditor("未审核");
        }
        vo.setImages(performance.getPerformanceImages().stream().map(PerformanceImage::getUrl).collect(Collectors.toList()));
        vo.setPerformanceType(performance.getPerformanceType().getUid());
        vo.setTypeName(performance.getPerformanceType().getName());
        return vo;
    }
}
