package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SuggestionAddDto;
import com.cdkhd.npc.entity.dto.SuggestionAuditDto;
import com.cdkhd.npc.entity.dto.SuggestionPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface SuggestionService {

    /**
     * 全部建议类型列表
     * */
    RespBody sugBusList(UserDetailsImpl userDetails);

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
     * 审核人员相关的建议
     * @param userDetails
     * @param dto
     * @return
     */
    RespBody auditorSug(UserDetailsImpl userDetails, SuggestionPageDto dto);

    //rfx
    //上级查看下级代表建议列表详情
    /**
     *  根据选择的代表查看其提出的建议
     * @param uid
     * @return
     */
    RespBody getMemberSugList(String uid, SuggestionPageDto dto);
}
