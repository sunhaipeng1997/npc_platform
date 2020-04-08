package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Performance;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class PerformanceListVo extends BaseVo {

   	//状态
	private Byte status;
	private String statusName;

   	//标题
	private String title;

   	//类型
    private String typeName;

   	//履职时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date workAt;

    public static PerformanceListVo convert(Performance performance) {
        PerformanceListVo vo = new PerformanceListVo();
        BeanUtils.copyProperties(performance, vo);
        vo.setTypeName(performance.getPerformanceType().getName());
        vo.setStatusName(performance.getStatus() == null?"未审核":performance.getStatus().equals(StatusEnum.ENABLED.getValue())?"审核通过":"驳回");
        return vo;
    }
}
