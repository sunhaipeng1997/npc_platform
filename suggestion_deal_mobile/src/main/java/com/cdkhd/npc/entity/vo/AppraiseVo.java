package com.cdkhd.npc.entity.vo;
/*
 * @description:建议评价
 * @author:liyang
 * @create:2020-06-05
 */

import com.cdkhd.npc.entity.Appraise;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Setter
@Getter
public class AppraiseVo extends BaseVo {

    //办理结果评分
    private Byte result;

    //办理态度评分
    private Byte attitude;

    //评价人姓名
    private String npcMemberName;

    //原因
    private String reason;

    public static AppraiseVo convert(Appraise appraise) {
        AppraiseVo appraiseVo = new AppraiseVo();
        BeanUtils.copyProperties(appraise, appraiseVo);
        appraiseVo.setNpcMemberName(appraise.getNpcMember().getName());
        return appraiseVo;
    }
}
