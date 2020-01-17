package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.News;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class NewsDetailsVo extends BaseVo {

    private String title;

    private String newsAbstract;

    //新闻详情页只需要主封面图
    private String coverUrl;

    private String smallCoverUrl;

    private String content;

    private String author;

    private String newsTypeUid;
    private String newsTypeName;

    private String tags;

    private Long readTimes;

    //是否直接显示链接原文
    private Boolean ShowOriginal;

    //文章的原文链接
    private String OriginalUrl;

    //新闻当前状态
    private Integer status;

    private Integer whereShow;

    private Boolean pushNews;

    private Long viewStatus;

    //审核人姓名
    private String reviewerName;

    private String feedback;

    //这个不一定为真实的发布时间，
    //是因为政府的业务需求，需要手动设置一个发布时间显示在移动端
    //以体现"及时性"
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    public static NewsDetailsVo convert(News news) {
        NewsDetailsVo vo = new NewsDetailsVo();

        BeanUtils.copyProperties(news, vo);
        vo.setNewsTypeUid(news.getNewsType().getUid());
        vo.setNewsTypeName(news.getNewsType().getName());


        //此审核人是实际审核该新闻的人，存储在NpcMember表中
//        因为数据库表的关联还没确定好，新闻审核人还没设置
//        vo.setReviewerName(news.getReviewer().getName());

        return vo;
    }
}
