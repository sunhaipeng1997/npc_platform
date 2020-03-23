package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.WorkStation;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Setter
@Getter
public class WorkStationPageVo extends BaseVo {

    //工作站名称
    private String name;

    //工作站介绍
    private String description;

    //工作站图片
    private String avatar;

    //工作站地理位置
    private String address;

    //联系人
    private String linkman;

    //联系电话
    private String telephone;

    // 是否可用
    private String enabled;

    //经度
    private String longitude;

    //纬度
    private String latitude;


    public static WorkStationPageVo convert(WorkStation workStation) {
        WorkStationPageVo vo = new WorkStationPageVo();
        BeanUtils.copyProperties(workStation, vo);
        vo.setEnabled(workStation.getEnabled() ? "可用" : "不可用");
        return vo;
    }
}
