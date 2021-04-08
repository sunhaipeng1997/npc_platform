package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Opinion;
import com.cdkhd.npc.enums.ReplayStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @描述 意见管理后端分页查询
 */
@Setter
@Getter
public class HomePageVo{

    //今日新增意见
    private Integer opinion;

    //今日新增建议
    private Integer suggestion;

    //今日新增
    private Integer performance;

}
