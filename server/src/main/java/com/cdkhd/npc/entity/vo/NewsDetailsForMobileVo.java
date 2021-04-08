package com.cdkhd.npc.entity.vo;


import com.cdkhd.npc.entity.News;
import com.cdkhd.npc.entity.NewsOpeRecord;
import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * 移动端审核人新闻详情
 */
@Getter
@Setter
public class NewsDetailsForMobileVo extends BaseVo{
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
    private String statusName;

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

    //操作记录
    private List<NewsOpeRecordVo> opeRecordList = new ArrayList<>();

    public static NewsDetailsForMobileVo convert(News news) {
        NewsDetailsForMobileVo vo = new NewsDetailsForMobileVo();

        BeanUtils.copyProperties(news, vo);
        vo.setNewsTypeUid(news.getNewsType().getUid());
        vo.setNewsTypeName(news.getNewsType().getName());

        vo.setStatusName(NewsStatusEnum.values()[news.getStatus()].getName());

        //将操作记录一并返回
        List<NewsOpeRecord> opeRecords = news.getOpeRecords();
        if(!opeRecords.isEmpty()){
            for(NewsOpeRecord opeRecord : opeRecords){
                NewsOpeRecordVo opeRecordVo = NewsOpeRecordVo.convert(opeRecord);
                vo.getOpeRecordList().add(opeRecordVo);
            }
        }

        return vo;
    }
}
