package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class NewsAddDto extends BaseDto {

    private String title;

    private String newsAbstract;

    private String coverUrl;

    private String smallCoverUrl;

    private String content;

    private String author;

    private String newsTypeUid;

    private String tags;

    private Integer whereShow;

    //移动端是否推送该新闻
    private Boolean pushNews;

    //是否直接显示链接原文
    private Boolean ShowOriginal;

    //文章的原文链接
    private String OriginalUrl;

    //这个不一定为真实的发布时间，
    //是因为政府的业务需求，需要手动设置一个发布时间显示在移动端
    //以体现"及时性"
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date publishAt;
}
