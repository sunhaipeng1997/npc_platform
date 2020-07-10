package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.LevelDto;
import com.cdkhd.npc.vo.RespBody;

public interface UnitService {

    /**
     * 获取单位下拉列表
     * @param userDetails
     * @return
     */
    RespBody unitList(MobileUserDetailsImpl userDetails, LevelDto levelDto);

}
