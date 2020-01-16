package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.News;
import com.cdkhd.npc.entity.NewsType;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.dto.NewsAddDto;
import com.cdkhd.npc.entity.dto.NewsReviewDto;
import com.cdkhd.npc.entity.dto.NewsPageDto;
import com.cdkhd.npc.entity.dto.UploadPicDto;
import com.cdkhd.npc.entity.vo.NewsDetailsVo;
import com.cdkhd.npc.entity.vo.NewsPageVo;
import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NewsService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.IOException;
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

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository, NpcMemberRepository npcMemberRepository, LoginUPRepository loginUPRepository, NewsTypeRepository newsTypeRepository) {
        this.newsRepository = newsRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.loginUPRepository = loginUPRepository;
        this.newsTypeRepository = newsTypeRepository;
    }

    @Override
    public RespBody add(UserDetailsImpl userDetails, NewsAddDto dto){
        RespBody body = new RespBody();

        News news = new News();
        BeanUtils.copyProperties(dto, news);
        news.setArea(userDetails.getArea());
        news.setTown(userDetails.getTown());
        news.setLevel(userDetails.getLevel());

        NewsType newsType = newsTypeRepository.findByUid(dto.getNewsTypeUid());
        if(newsType == null){
            body.setMessage("该新闻栏目不存在");
            body.setStatus(HttpStatus.NOT_FOUND);
            LOGGER.warn("uid为{}的该新闻栏目不存在,创建新闻失败",dto.getNewsTypeUid());
            return body;
        }

        news.setNewsType(newsType);

        //保存数据
        newsRepository.save(news);

        body.setMessage("添加新闻成功");
        return body;
    }

    @Override
    public RespBody uploadImage(UploadPicDto dto) {
        RespBody<JSONObject> body = new RespBody<>();
        JSONObject jsonObj = new JSONObject();

        MultipartFile image = dto.getImage();
        if (image == null) {
            body.setMessage("封面不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            LOGGER.warn("新闻封面不能为空,上传封面图失败",dto.getUid());
            return body;
        }

        Integer width = dto.getWidth(),height = dto.getHeight();
        //下面对图片进行压缩
        if(dto.getWidth()==null || dto.getHeight() == null){
            width = 600;
            height = 400;
        }

        //用于新闻详情页和首页轮播位置，缩略图，宽长比为1.5
        String bigCoverUrl = ImageUploadUtil.saveImage("news",image,width,height);
        if(bigCoverUrl.equals("error")){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("服务器错误");
            LOGGER.warn("保存新闻大封面图片失败");
            return body;
        }else {
            jsonObj.put("bigCoverUrl", bigCoverUrl);
        }

        //新闻列表位置显示的小尺寸缩略图
//        String smallCoverUrl = ImageUploadUtil.saveImage("news",image,180,140);
//        if(smallCoverUrl.equals("error")){
//            body.setStatus(HttpStatus.BAD_REQUEST);
//            body.setMessage("服务器错误");
//            LOGGER.warn("保存新闻小封面图片失败");
//            return body;
//        }else {
//            jsonObj.put("smallCoverUrl", smallCoverUrl);
//        }

        body.setData(jsonObj);
        return body;
    }

    @Override
    public RespBody delete(String uid) {
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(uid);
        if(news == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，删除新闻失败",uid);
            return body;
        }

        //如果新闻在审核中，则不允许删除
        if(news.getStatus() == NewsStatusEnum.UNDER_REVIEW.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该新闻在审核中，不能删除");
            LOGGER.warn("uid为 {} 的新闻在审核中，删除新闻失败",uid);
            return body;
        }

        newsRepository.deleteByUid(uid);

        body.setMessage("删除新闻成功");
        return body;
    }

    @Override
    public RespBody update(NewsAddDto dto) {
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(dto.getUid());
        if(news == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，更新新闻失败",dto.getUid());
            return body;
        }

        //状态为“审核中”的新闻不可以修改编辑
        //其他状态均可修改(编辑)，修改后状态变为"草稿"，并且重新进行后面的审核流程
        if(news.getStatus() == NewsStatusEnum.UNDER_REVIEW.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该新闻在审核中，不能修改编辑");
            LOGGER.warn("uid为 {} 的新闻在审核中，更新新闻失败",dto.getUid());
            return body;
        }

        //如果用户有重新上传封面图片，则删除之前的图片
        String oldCover = news.getCoverUrl();
        if (StringUtils.isNotEmpty(oldCover) && !dto.getCoverUrl().equals(oldCover)) {
            File oldCoverFile = new File("static", oldCover);
            if (oldCoverFile.exists()) {
                try {
                    FileUtils.forceDelete(oldCoverFile);
                } catch (IOException e) {
                    LOGGER.error("新闻封面图片删除失败 {}", e);
                    body.setStatus(HttpStatus.BAD_REQUEST);
                    body.setMessage("系统内部错误");
                    return body;
                }
            }
        }

        BeanUtils.copyProperties(dto, news);

        //修改后状态变为"草稿"，并且重新进行后面的审核流程
        news.setStatus(NewsStatusEnum.DRAFT.ordinal());

        newsRepository.saveAndFlush(news);
        body.setMessage("修改新闻成功");
        return body;
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
     * 后台管理员提交新闻审核
     *
     * @param userDetails 用户信息
     * @param uid   新闻uid
     * @return
     */
    @Override
    public RespBody toReview(UserDetailsImpl userDetails,String uid){
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(uid);
        if(news == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，不能提交审核",uid);
            return body;
        }

        //后台管理员可以在新闻创建后、被退回并修改后在再提交，此时状态为：DRAFT
        //也可以直接将审核不通过的新闻再次提交审核，此时新闻状态为：NOT_APPROVED
        if(news.getStatus() != NewsStatusEnum.DRAFT.ordinal() &&
           news.getStatus() != NewsStatusEnum.NOT_APPROVED.ordinal()){

            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("在[审核中][待发布][已发布]状态均不能提交审核");
            LOGGER.warn("uid为 {} 的新闻不处于[草稿]或者[审核不通过]状态，固不能提交审核",uid);
            return body;
        }

        //查找与本账号同地区/镇的具有新闻审核权限的用户

        //将状态设置为"审核中"
        news.setStatus(NewsStatusEnum.UNDER_REVIEW.ordinal());
        newsRepository.save(news);

        //推送消息

        body.setMessage("成功提交新闻审核");
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
        Account currentAccount = loginUPRepository.findByUsername(userDetails.getUsername()).getAccount();
        news.setReviewer(npcMemberRepository.findByAccount(currentAccount));

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
