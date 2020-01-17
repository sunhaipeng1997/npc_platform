package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.entity.WorkStation;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

@Setter
@Getter
public class WorkStationAddDto {

    private String uid;

    //工作站名称
    private String name;

    //工作站介绍
    private String description;

    //工作站图片
    private String avatar;

    //工作站地理位置
    private String address;

    //经度
    private String longitude;

    //纬度
    private String latitude;

    //联系人
    private String linkman;

    //联系电话
    private String telephone;

    public WorkStation convert() {
        WorkStation station = new WorkStation();
        if (StringUtils.isBlank(this.uid)) {
            this.uid = station.getUid();
        }
        BeanUtils.copyProperties(this, station);
        return station;
    }
}
