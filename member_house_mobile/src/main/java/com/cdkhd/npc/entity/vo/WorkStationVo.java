package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.WorkStation;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Setter
@Getter
public class WorkStationVo extends BaseVo {

    //名称
    private String name;

    //工作站图片url
    private String avatar;

    private String address;

    private String description;

    private String latitude;

    private String longitude;

    public static WorkStationVo convert(WorkStation workStation) {
        WorkStationVo vo = new WorkStationVo();
        BeanUtils.copyProperties(workStation, vo);
        return vo;
    }
}
