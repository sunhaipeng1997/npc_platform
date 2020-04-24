package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;
import javax.persistence.*;
import com.cdkhd.npc.enums.NewsStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Setter
@Getter
@ToString
@Entity
@Table ( name ="news" )
public class News extends BaseDomain {

	//新闻标题
	@Column(nullable = false)
	private String title;

	//文章摘要，也可以当做小标题(二级标题)
	@Column(name = "news_abstract" )
	private String newsAbstract;

	//主封面图
	//用在[新闻详情页]和[作为轮播新闻时]两个地方
	@Column(name = "cover_url" )
	private String coverUrl;

	//小尺寸缩略图
	//用在首页新闻列表中，这个地方需要更小尺寸的缩略图，以尽可能减少新闻列表请求的时间
	//和封面图可以不一样
	@Column(name = "small_cover_url" )
	private String smallCoverUrl;

	//正文内容-是HTML富文本，将其设置为字符串大对象，并懒加载
	@Lob
	@Basic(fetch = FetchType.LAZY)
   	@Column(nullable = false)
	private String content;

	//作者(发布单位)
	@Column(name = "author" )
	private String author;

   	//浏览量
	@Column(name = "read_times" )
	private Long readTimes = 0L;

	//所属类型(所属栏目)
	@ManyToOne(targetEntity = NewsType.class,fetch = FetchType.LAZY)
	@JoinColumn(name = "news_type", referencedColumnName = "id")
	private NewsType newsType;

	//展示在移动端的位置(优先级)，比如列表(常规0)、置顶(1)、轮播(2)
	@Column(name = "where_show" )
	private Integer whereShow;

	@Column(name = "push_news" )
	private Boolean pushNews=false;

	//新闻标签
	@Column(name = "tags" )
	private String tags;

	//是否直接显示链接原文
	@Column(name = " show_original" )
	private Boolean ShowOriginal=false;

	//文章的原文链接
	@Column(name = " original_url" )
	private String OriginalUrl;

	//新闻状态
	@Column(name = "status" )
	private Integer status = NewsStatusEnum.DRAFT.ordinal();


   	//表示是否公开的，暂时不合并到status中,因为有时会将某些已发布新闻隐藏，不再公开，
   	@Column(name = "published" )
	private Boolean published = false;

	//公开发布时间
    @Column(name = "publish_at" )
    @Temporal(TemporalType.TIMESTAMP)
	private Date publishAt;

	@Column(name = "level" )
   	private Byte level;

   	//关联新闻审核人
   	//指的是具体对该新闻进行审核操作的人
	@ManyToOne(targetEntity = NpcMember.class,fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer", referencedColumnName = "id")
	private NpcMember reviewer;

	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Area area;

	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "town", referencedColumnName = "id")
	private Town town;

	//审核人是否有查看该新闻
	@Column(name = "view" )
	private Boolean view = false;

	//记录各位审核人或后台管理员对通知的操作记录
	@OneToMany(targetEntity = NewsOpeRecord.class, mappedBy = "news", orphanRemoval = true, cascade = CascadeType.ALL)
	private List<NewsOpeRecord> opeRecords = new ArrayList<>();
}
