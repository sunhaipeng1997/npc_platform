package com.cdkhd.npc.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewDto {

    //查看的建议uid
    private String sugUid;

    //当前代表等级
    private Byte level;

    //1、自己点击的，查看审核人回复时候消除未读
    //2、审核人点击的，查看新提交的数据的时候消除未读
    private Byte type;

    //是否应当将doneView字段置为true
    private Boolean changeDoneView;

    //是否应当将该建议对应的附议中属于该代表的附议的view字段设置为true
    private Boolean changeSecondView;

}
