package com.cdkhd.npc.service;
/*
 * @description:政府模块业务层接口
 * @author:liyang
 * @create:2020-05-20
 */

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.GovAddDto;
import com.cdkhd.npc.vo.RespBody;

public interface GovService {

    RespBody addGovernment(UserDetailsImpl userDetails, GovAddDto govAddDto);

    RespBody updateGovernment(GovAddDto govAddDto);

    RespBody detailGovernment(UserDetailsImpl userDetails);
}
