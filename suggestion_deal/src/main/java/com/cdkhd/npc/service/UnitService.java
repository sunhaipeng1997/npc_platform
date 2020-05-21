package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.UnitAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitPageDto;
import com.cdkhd.npc.entity.dto.UnitUserAddOrUpdateDto;
import com.cdkhd.npc.entity.dto.UnitUserPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface UnitService {

    //单位基本信息维护

    /**
     * 分页查询单位
     * @param userDetails
     * @param unitPageDto
     * @return
     */
    RespBody unitPage(UserDetailsImpl userDetails,UnitPageDto unitPageDto);

    /**
     * 添加或修改单位信息
     * @param userDetails
     * @param unitAddOrUpdateDto
     * @return
     */
    RespBody addOrUpdateUnit(UserDetailsImpl userDetails,UnitAddOrUpdateDto unitAddOrUpdateDto);

    /**
     * 单位详细信息
     * @param uid
     * @return
     */
    RespBody unitDetails(String uid);

    /**
     * 删除单位
     * @param uid
     * @return
     */
    RespBody deleteUnit(String uid);

    /**
     * 修改单位状态
     * @param uid
     * @param status
     * @return
     */
    RespBody changeUnitStatus(String uid, Byte status);

    /**
     * 获取单位下拉列表
     * @param userDetails
     * @return
     */
    RespBody unitList(UserDetailsImpl userDetails);

    //单位人员信息维护

    /**
     * 分页查询单位人员信息
     * @param userDetails
     * @param unitUserPageDto
     * @return
     */
    RespBody unitUserPage(UserDetailsImpl userDetails,UnitUserPageDto unitUserPageDto);

    /**
     * 添加或修改单位人员信息
     * @param userDetails
     * @param unitUserAddOrUpdateDto
     * @return
     */
    RespBody addOrUpdateUnitUser(UserDetailsImpl userDetails,UnitUserAddOrUpdateDto unitUserAddOrUpdateDto);

    /**
     * 单位人员详细信息
     * @param uid
     * @return
     */
    RespBody unitUserDetails(String uid);

    /**
     * 删除单位人员信息
     * @param uid
     * @return
     */
    RespBody deleteUnitUser(String uid);

    //重置单位人员的登录密码
    RespBody resetPwd(String uid);
}
