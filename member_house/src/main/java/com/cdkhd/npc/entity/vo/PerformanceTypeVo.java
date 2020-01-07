package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.BaseDomain;
import com.cdkhd.npc.entity.PerformanceType;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */
@Getter
@Setter
public class PerformanceTypeVo extends BaseVo {

    //类型名称
	private String name;

    //类型状态
    private Byte status;
    private String statusName;

    //排序
    private Integer sequence;

    //说明
    private String remark;

    public static PerformanceTypeVo convert(PerformanceType performanceType) {
        PerformanceTypeVo vo = new PerformanceTypeVo();
        BeanUtils.copyProperties(performanceType, vo);
        vo.setStatusName(StatusEnum.getName(performanceType.getStatus()));
        return vo;
    }
}
