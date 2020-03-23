package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.News;
import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
public class NewsPageVo extends BaseVo {

    private String title;

    private String newsAbstract;

    private String coverUrl;

    private String smallCoverUrl;

    private String author;

    private String newsTypeName;

    private String tags;

    private Long readTimes;

    private Integer whereShow;

    private Integer status;
    private String statusName;

    //这个不一定为真实的发布时间，
    //是因为政府的业务需求，需要手动设置一个发布时间显示在移动端
    //以体现"及时性"
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    public static NewsPageVo convert(News news) {
        NewsPageVo vo = new NewsPageVo();

        BeanUtils.copyProperties(news, vo);
        vo.setNewsTypeName(news.getNewsType().getName());
        vo.setStatusName(NewsStatusEnum.values()[news.getStatus()].getName());

        return vo;
    }

}
