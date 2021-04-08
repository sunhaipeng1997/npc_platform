package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Attachment;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;


@Setter
@Getter
public class AttachmentVo extends BaseVo {
    private String url;

    private String name;

    public static AttachmentVo convert(Attachment attachment) {
        AttachmentVo vo = new AttachmentVo();
        BeanUtils.copyProperties(attachment, vo);
        return vo;
    }
}
