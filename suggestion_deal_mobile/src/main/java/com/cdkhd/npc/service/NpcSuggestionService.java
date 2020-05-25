package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.SugAddDto;
import com.cdkhd.npc.entity.dto.SugAuditDto;
import com.cdkhd.npc.entity.dto.SugBusDto;
import com.cdkhd.npc.entity.dto.SugPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface NpcSuggestionService {

    /**
    * @Description: 建议类型下拉列表
    * @Param:
    * @Return:
    * @Date: 2020/5/19
    * @Author: LiYang
    */
    RespBody sugBusList(MobileUserDetailsImpl userDetails, SugBusDto sugBusDto);

    /**
    * @Description: 代表添加建议
    * @Param:
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    RespBody addSuggestion(MobileUserDetailsImpl userDetails, SugAddDto sugAddDto);

    /**
    * @Description:  代表修改建议
    * @Param:
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    RespBody updateSuggestion(SugAddDto sugAddDto);

    /**
     * @Description: 提交建议
     * @Param:
     * @Return:
     * @Date: 2020/5/18
     * @Author: LiYang
     */
    RespBody submitSuggestion(SugAddDto sugAddDto);

    /**
    * @Description: 撤回建议
    * @Param:
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    RespBody revokeSuggestion(String sugUid);

    /**
    * @Description: 查看建议详情
    * @Param:
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    RespBody suggestionDetail(String sugUid);

    /**
    * @Description: 删除建议
    * @Param: sugUid
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    RespBody deleteSuggestion(String sugUid);

    /**
    * @Description: 审核建议
    * @Param:
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    RespBody auditSuggestion(MobileUserDetailsImpl userDetails, SugAuditDto sugAuditDto);

    /**
    * @Description: 查看代表建议列表
    * @Param:
    * @Return:
    * @Date: 2020/5/18
    * @Author: LiYang
    */
    RespBody npcMemberSug(MobileUserDetailsImpl userDetails, SugPageDto sugPageDto);

    /**
    * @Description: s很合人员建议列表
    * @Param:
    * @Return:
    * @Date: 2020/5/19
    * @Author: LiYang
    */
    RespBody auditorSug(MobileUserDetailsImpl userDetails, SugPageDto sugPageDto);

}
