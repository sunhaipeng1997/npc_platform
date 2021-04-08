package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewDto extends BaseDto {

    //1、自己点击的，查看审核人回复时候消除未读

    //2、审核人点击的，查看新提交的数据的时候消除未读
    private Byte type;

}
