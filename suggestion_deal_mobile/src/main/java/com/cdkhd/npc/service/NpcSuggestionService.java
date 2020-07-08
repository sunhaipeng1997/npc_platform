package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    RespBody suggestionDetail(MobileUserDetailsImpl userDetails, ViewDto viewDto);

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
    * @Description: 审核人员建议列表
    * @Param:
    * @Return:
    * @Date: 2020/5/19
    * @Author: LiYang
    */
    RespBody auditorSug(MobileUserDetailsImpl userDetails, SugPageDto sugPageDto);

    /**
    * @Description: 代表接受办理结果并评价
    * @Param:
    * @Return:
    * @Date: 2020/5/26
    * @Author: LiYang
    */
    RespBody acceptResult(MobileUserDetailsImpl userDetails, SugAppraiseDto sugAppraiseDto);


    /**
    * @Description: 代表不接受办理结果
    * @Param:
    * @Return:
    * @Date: 2020/5/26
    * @Author: LiYang
    */
    RespBody refuseResult(SugAppraiseDto sugAppraiseDto);

    /**
    * @Description: 代表附议建议
    * @Param:
    * @Return:
    * @Date: 2020/5/28
    * @Author: LiYang
    */
    RespBody secondSuggestion(MobileUserDetailsImpl userDetails, SugSecondDto sugSecondDto);

    /**
    * @Description: 代表催办建议
    * @Param:
    * @Return:
    * @Date: 2020/5/29
    * @Author: LiYang
    */
    RespBody urgeSuggestion(MobileUserDetailsImpl userDetails, Byte level, String sugUid);

    /**
    * @Description: 获取建议的办理流程
    * @Param:
    * @Return:
    * @Date: 2020/6/8
    * @Author: LiYang
    */
    RespBody handleProcessDetail(String sugUid, Byte type);

    /**
     * @Description: 代表建议详情，生成doc文档
     * @Param:
     * @Return:
     * @Date: 2020/6/16
     * @Author: LiYang
     */
    String detailDoc(HttpServletRequest req, HttpServletResponse res, String sugUid);
}
