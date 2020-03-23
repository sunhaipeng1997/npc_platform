package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.News;
import com.cdkhd.npc.entity.dto.NewsPageDto;
import com.cdkhd.npc.entity.dto.NewsReviewDto;
import com.cdkhd.npc.entity.vo.NewsDetailsVo;
import com.cdkhd.npc.entity.vo.NewsPageVo;
import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NewsService;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl implements NewsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsServiceImpl.class);

    private NewsRepository newsRepository;
    private NpcMemberRepository npcMemberRepository;
    private LoginUPRepository loginUPRepository;
    private NewsTypeRepository newsTypeRepository;
    private AccountRepository accountRepository;

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository, NpcMemberRepository npcMemberRepository, LoginUPRepository loginUPRepository, NewsTypeRepository newsTypeRepository, AccountRepository accountRepository) {
        this.newsRepository = newsRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.loginUPRepository = loginUPRepository;
        this.newsTypeRepository = newsTypeRepository;
        this.accountRepository = accountRepository;
    }


    /**
     * 分页查询
     * @param userDetails 用户信息
     * @param pageDto 新闻页面dto
     * @return
     */
    @Override
    public RespBody page(UserDetailsImpl userDetails, NewsPageDto pageDto){

        //分页查询条件
        int begin = pageDto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()),
                pageDto.getProperty());

        //用户查询条件
        Specification<News> specification = (root, query, cb)->{
            List<Predicate> predicateList = new ArrayList<>();

            //按地区编码查询
            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));

            //按镇/社区名称模糊查询
            if(userDetails.getTown() != null){
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
            }

            //按栏目查询
            if (StringUtils.isNotEmpty(pageDto.getNewsTypeName())) {
                predicateList.add(cb.equal(root.get("newsType").get("name").as(String.class),  pageDto.getNewsTypeName()));
            }

            //按新闻标题模糊查询
            if (StringUtils.isNotEmpty(pageDto.getTitle())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + pageDto.getTitle() + "%"));
            }

            //按新闻作者模糊查询
            if (StringUtils.isNotEmpty(pageDto.getAuthor())) {
                predicateList.add(cb.like(root.get("author").as(String.class), "%" + pageDto.getAuthor() + "%"));
            }

            //按新闻状态查询
            if (pageDto.getStatus() != null) {
                predicateList.add(cb.equal(root.get("status").as(Integer.class), pageDto.getStatus()));
            }

            //按显示位置/优先级查询
            if(pageDto.getWhereShow() != null){
                predicateList.add(cb.equal(root.get("whereShow").as(Integer.class), pageDto.getWhereShow()));
            }

            return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };

        //查询数据库
        Page<News> page = newsRepository.findAll(specification,pageable);

        //封装查询结果
        PageVo<NewsPageVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(page.getContent().stream().map(NewsPageVo::convert).collect(Collectors.toList()));

        //返回数据
        RespBody<PageVo> body = new RespBody<>();
        body.setData(pageVo);

        return body;
    }


    /**
     * 获取某一新闻的细节
     *
     * @param uid 新闻uid
     * @return
     */
    @Override
    public RespBody details(String uid){
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(uid);
        if(news == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，不能提交审核",uid);
            return body;
        }

        NewsDetailsVo vo = NewsDetailsVo.convert(news);
        body.setData(vo);

        body.setMessage("成功获取新闻细节");
        return body;
    }

    /**
     * 审核人对新闻进行审核
     * @param userDetails 用户信息
     * @param dto 新闻审核参数封装对象
     * @return
     */
    @Override
    public RespBody review(UserDetailsImpl userDetails,NewsReviewDto dto){
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(dto.getUid());
        if (news == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("指定的新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，审核新闻失败",dto.getUid());
            return body;
        }

        if(news.getStatus() != NewsStatusEnum.UNDER_REVIEW.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("指定的新闻不在[审核中]状态");
            LOGGER.warn("uid为 {} 的新闻不在[审核中]状态，审核新闻失败",dto.getUid());
            return body;
        }

        //如果审核结果为:通过
        if(dto.isPass()){
            //将新闻状态设置为"待发布"(可发布)状态
            news.setStatus(NewsStatusEnum.RELEASABLE.ordinal());
        }else {
            //如果审核结果为:不通过

            //将新闻状态设置为"不通过"状态
            news.setStatus(NewsStatusEnum.NOT_APPROVED.ordinal());
        }

        //对新闻的反馈意见
        news.setFeedback(dto.getFeedback());

        //将当前用户记录为该新闻的审核人
        Account currentAccount = accountRepository.findByUid(userDetails.getUsername());

        news.setReviewer(NpcMemberUtil.getCurrentIden(userDetails.getLevel(),currentAccount.getNpcMembers()));

        newsRepository.saveAndFlush(news);

        body.setMessage("完成新闻审核");
        return body;
    }

    /**
     * 后台管理员 或者 新闻审核人 将新闻公开
     *
     * @param uid 新闻uid
     * @return
     */
    @Override
    public RespBody publish(String uid){
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(uid);

        if (news == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("指定的新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，发布新闻失败",uid);
            return body;
        }

        if(news.getStatus() != NewsStatusEnum.RELEASABLE.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该新闻还未审核通过，不可发布");
            LOGGER.warn("uid为 {} 的新闻还未审核通过，发布新闻失败",uid);
            return body;
        }

        if(news.getPublished()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该新闻已经公开，不可重复公开");
            LOGGER.warn("uid为 {} 的新闻已经公开，不可重复设置为公开",uid);
            return body;
        }

        //将状态设置为已发布
        news.setStatus(NewsStatusEnum.RELEASED.ordinal());

        //将新闻设置为公开状态
        news.setPublished(true);

        newsRepository.saveAndFlush(news);

        body.setMessage("新闻公开发布成功");
        return body;
    }

}
