package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NewsType;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class NewsTypeVo extends BaseVo {

    private String name;

    private Integer area;

    private String town;

    //类型状态
    private String status;

    //类型顺序
    private String sequence;

    private String remark;

    public static NewsTypeVo convert(NewsType newsType){
        NewsTypeVo vo = new NewsTypeVo();

        BeanUtils.copyProperties(newsType,vo);

        return vo;
    }
}
