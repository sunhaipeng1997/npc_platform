package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionAddDto;
import com.cdkhd.npc.entity.dto.SuggestionAuditDto;
import com.cdkhd.npc.entity.dto.SuggestionPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface SuggestionService {


    //建议业务类型相关接口
    /**
     * 全部建议类型列表
     * */
    RespBody sugBusList(UserDetailsImpl userDetails);




    //小程序建议相关接口
    /**
     * 代表提出的建议
     * @param userDetails
     * @param dto
     * @return
     */
    RespBody npcMemberSug(UserDetailsImpl userDetails, SuggestionPageDto dto);

    /**
     * 添加/修改 建议
     * @param userDetails
     * @param dto
     * @return
     */
    RespBody addOrUpdateSuggestion(UserDetailsImpl userDetails, SuggestionAddDto dto);


    /**
     * 审核（回复）建议
     * @param
     * @param
     * @return
     */
    RespBody audit(UserDetailsImpl userDetails, SuggestionAuditDto suggestionAuditDto);


    /**
     * 删除建议
     * @param uid
     * @return
     */
    RespBody deleteSuggestion(String uid);


    /**
     * 建议详情
     * @param uid
     * @return
     */
    String suggestionDetail(String uid);


    RespBody suggestionRevoke(String uid);


    /**
     * 建议排名
     * @param userDetails
     * @return
     */
    RespBody rank(UserDetailsImpl userDetails);

    /**
     * 审核人员相关的建议
     * @param userDetails
     * @param dto
     * @return
     */
    RespBody auditorSug(UserDetailsImpl userDetails, SuggestionPageDto dto);
}
