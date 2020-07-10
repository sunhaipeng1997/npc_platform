package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionBusinessAddDto;
import com.cdkhd.npc.entity.dto.SuggestionBusinessDto;
import com.cdkhd.npc.vo.RespBody;

public interface SuggestionBusinessService {

    //建议业务类型相关接口
    /**
     * 全部建议业务类型下拉列表
     * */
    RespBody sugBusList(UserDetailsImpl userDetails);

    /**
     * 某下属镇建议业务类型下拉列表
     * */
    RespBody subTownBusList(String townUid);

    /**
     * 条件查询建议业务类型
     * @param userDetails
     * @param suggestionBusinessDto
     * @return
     */
    RespBody findSuggestionBusiness(UserDetailsImpl userDetails, SuggestionBusinessDto suggestionBusinessDto);

    /**
     * 添加、修改建议业务类型
     * @return
     */
    RespBody addOrUpdateSuggestionBusiness(UserDetailsImpl userDetails, SuggestionBusinessAddDto suggestionBusinessAddDto);

    /**
     * 删除建议业务类型
     * @param uid
     * @return
     */
    RespBody deleteSuggestionBusiness(String uid);

    /**
     * 修改业务类型排序
     * @param uid
     * @param type
     * @return
     */
    RespBody changeTypeSequence(UserDetailsImpl userDetails, String uid, Byte type);

    /**
     * 修改业务类型状态
     * @param uid
     * @param status
     * @return
     */
    RespBody changeBusinessStatus(String uid, Byte status);

}
