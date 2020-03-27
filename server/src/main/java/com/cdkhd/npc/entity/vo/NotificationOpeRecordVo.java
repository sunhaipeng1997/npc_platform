package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Notification;
import com.cdkhd.npc.entity.NotificationOpeRecord;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.enums.NotificationStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class NotificationOpeRecordVo extends BaseVo {
    private Integer originalStatus;
    private String originalStatusName;

    private Integer resultStatus;
    private String resultStatusName;

    private String feedback;

    //操作人姓名
    private String operatorName;

    public static NotificationOpeRecordVo convert(NotificationOpeRecord opeRecord) {
        NotificationOpeRecordVo vo = new NotificationOpeRecordVo();

        BeanUtils.copyProperties(opeRecord, vo);
        vo.setOriginalStatusName(NotificationStatusEnum.values()[opeRecord.getOriginalStatus()].getName());
        vo.setResultStatusName(NotificationStatusEnum.values()[opeRecord.getResultStatus()].getName());

        vo.setOperatorName(opeRecord.getOperator().getName());
        return vo;
    }
}
