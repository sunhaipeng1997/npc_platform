package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageVo extends BaseVo {
    private String name;

    private String url;

    //总的大小
    private Long uploadTotal;
    //已上传大小
    private Long  uploaded;
}
