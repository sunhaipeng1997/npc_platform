package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NewsType;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class NewsTypeVo extends BaseVo {

    private String name;

    //栏目状态
    private Byte status;
    private String statusName;

    //栏目顺序
    private Integer sequence;

    private String remark;

    public static NewsTypeVo convert(NewsType newsType){
        NewsTypeVo vo = new NewsTypeVo();
        BeanUtils.copyProperties(newsType,vo);
        vo.setStatusName(StatusEnum.getName(newsType.getStatus()));
        return vo;
    }
}
