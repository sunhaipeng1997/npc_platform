package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Session;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
public class SessionVo extends BaseVo {

    //届期名称
    private String name;

    //开始日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    //结束日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    //描述
    private String remark;

    //是否本届
    private Boolean isCurrent;

    public static SessionVo convert(Session session) {
        SessionVo vo = new SessionVo();
        BeanUtils.copyProperties(session, vo);
        vo.setName(session.getIsCurrent() ? session.getName() + "（本届）" : session.getName());
        return vo;
    }
}
