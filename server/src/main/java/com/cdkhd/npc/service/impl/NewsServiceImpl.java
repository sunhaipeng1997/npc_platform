package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.ImageVo;
import com.cdkhd.npc.entity.vo.NewsDetailsForMobileVo;
import com.cdkhd.npc.entity.vo.NewsDetailsVo;
import com.cdkhd.npc.entity.vo.NewsPageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.MsgTypeEnum;
import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.enums.NpcMemberRoleEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NewsService;
import com.cdkhd.npc.service.NpcMemberRoleService;
import com.cdkhd.npc.service.PushMessageService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NewsServiceImpl implements NewsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsServiceImpl.class);

    private NewsRepository newsRepository;
    private NpcMemberRepository npcMemberRepository;
    private LoginUPRepository loginUPRepository;
    private NewsTypeRepository newsTypeRepository;
    private AccountRepository accountRepository;
    private NewsOpeRecordRepository newsOpeRecordRepository;
    private NpcMemberRoleService npcMemberRoleService;
    private PushMessageService pushMessageService;

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository, NpcMemberRepository npcMemberRepository, LoginUPRepository loginUPRepository, NewsTypeRepository newsTypeRepository, AccountRepository accountRepository, NewsOpeRecordRepository newsOpeRecordRepository, NpcMemberRoleService npcMemberRoleService, PushMessageService pushMessageService) {
        this.newsRepository = newsRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.loginUPRepository = loginUPRepository;
        this.newsTypeRepository = newsTypeRepository;
        this.accountRepository = accountRepository;
        this.newsOpeRecordRepository = newsOpeRecordRepository;
        this.npcMemberRoleService = npcMemberRoleService;
        this.pushMessageService = pushMessageService;
    }

    @Override
    public RespBody uploadImage(UploadPicDto dto) {
//        RespBody<JSONObject> body = new RespBody<>();
//        JSONObject jsonObj = new JSONObject();

        RespBody<ImageVo> body = new RespBody<>();

        MultipartFile image = dto.getImage();

        if (image == null) {
            body.setMessage("图片不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            LOGGER.warn("图片不能为空,上传图片失败",dto.getUid());
            return body;
        }

        String imgUrl;
        String orgName = image.getOriginalFilename();

        //下面对图片尺寸进行压缩
        if(dto.getWidth()==null || dto.getHeight() == null){
            imgUrl = ImageUploadUtil.saveImage("news",image);
        }else{
            imgUrl = ImageUploadUtil.saveImage("news",image,dto.getWidth(),dto.getHeight());
        }

        if(imgUrl.equals("error")){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("服务器错误");
            LOGGER.warn("保存图片失败");
            return body;
        }else {

            ImageVo vo = new ImageVo();
            vo.setName(orgName);
            vo.setUrl(imgUrl);
            vo.setUploadTotal(image.getSize());
            vo.setUploaded(image.getSize());
            body.setData(vo);
            body.setMessage("上传成功");
            body.setStatus(HttpStatus.OK);
//            jsonObj.put("url", imgUrl);
        }

//        body.setData(jsonObj);
        return body;
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

        //TODO 删除对应的封面图

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
        //暂时不管，不去删除，免得引起bug
//        if(StringUtils.isNotEmpty(dto.getCoverUrl())){
//            String oldCover = news.getCoverUrl();
//            if (!oldCover.isEmpty() && (!dto.getCoverUrl().equals(oldCover))) {
//                File oldCoverFile = new File("static", oldCover);
//                if (oldCoverFile.exists()) {
//                    try {
//                        FileUtils.forceDelete(oldCoverFile);
//                    } catch (IOException e) {
//                        LOGGER.error("新闻封面图片删除失败 {}", e);
//                        body.setStatus(HttpStatus.BAD_REQUEST);
//                        body.setMessage("系统内部错误");
//                        return body;
//                    }
//                }
//            }
//        }

        BeanUtils.copyProperties(dto, news);

        //修改后状态变为"草稿"，并且重新进行后面的审核流程
        news.setStatus(NewsStatusEnum.DRAFT.ordinal());
        news.setView(false);
        newsRepository.saveAndFlush(news);
        body.setMessage("修改新闻成功");
        return body;
    }

    /**
     * 后台管理员 或者 新闻审核人 将新闻公开
     *
     * @param dto 新闻uid
     * @return
     */
    @Override
    public RespBody publish(UserDetailsImpl userDetails, BaseDto dto){
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(dto.getUid());

        if (news == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("指定的新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，发布新闻失败",dto.getUid());
            return body;
        }

        if(news.getStatus() != NewsStatusEnum.RELEASABLE.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该新闻还未审核通过，不可发布");
            LOGGER.warn("uid为 {} 的新闻还未审核通过，发布新闻失败",dto.getUid());
            return body;
        }

        if(news.getPublished()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该新闻已经公开，不可重复公开");
            LOGGER.warn("uid为 {} 的新闻已经公开，不可重复设置为公开",dto.getUid());
            return body;
        }

        //添加操作记录
        NewsOpeRecord newsOpeRecord = new NewsOpeRecord();
        newsOpeRecord.setOriginalStatus(news.getStatus());
        newsOpeRecord.setResultStatus(NewsStatusEnum.RELEASED.ordinal());
        newsOpeRecord.setFeedback("完成发布"+news.getTitle());
        newsOpeRecord.setOpTime(new Date());
        newsOpeRecord.setAction("发布");
        //将调用该接口的当前用户记录为该新闻的(操作者)
        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        newsOpeRecord.setOperator(currentAccount.getUsername());
        newsOpeRecord.setNews(newsRepository.findByUid(news.getUid()));
        newsOpeRecordRepository.saveAndFlush(newsOpeRecord);

        //将状态设置为已发布
        news.setStatus(NewsStatusEnum.RELEASED.ordinal());
        //将新闻设置为公开状态
        news.setPublished(true);
        news.getOpeRecords().add(newsOpeRecord);
        newsRepository.saveAndFlush(news);

        //如果这条新闻是需要推送的，群发消息
        if(news.getPushNews()){
            //构造消息
            JSONObject newsMsg = new JSONObject();
            newsMsg.put("subtitle","收到一条新闻");
            newsMsg.put("time",news.getPublishAt());
            newsMsg.put("theme",news.getTitle());
            newsMsg.put("remarkInfo","来源:"+news.getAuthor()+"<点击查看详情>");

            //查找与本账号相同地区及层级的代表
            List<NpcMember> receivers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
            for(NpcMember receiver:receivers){
                if(receiver.getAccount() != null){
                    if(receiver.getAccount().getLoginWeChat() != null){
                        pushMessageService.pushMsg(receiver.getAccount(),MsgTypeEnum.CONFERENCE.ordinal(),newsMsg);
                    }
                }
            }
        }

        body.setMessage("新闻公开发布成功");
        return body;
    }


    /**
     * PC端分页查询
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
            predicateList.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));

            //按镇/社区名称模糊查询
            if(userDetails.getTown() != null){
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
            }

            //按栏目查询
            if (StringUtils.isNotEmpty(pageDto.getNewsType())) {
                predicateList.add(cb.equal(root.get("newsType").get("uid").as(String.class),  pageDto.getNewsType()));
            }

            //按新闻标题模糊查询
            if (StringUtils.isNotEmpty(pageDto.getTitle())) {
                predicateList.add(cb.like(root.get("title").as(String.class), "%" + pageDto.getTitle() + "%"));
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
        news.setReadTimes(news.getReadTimes() + 1);
        newsRepository.saveAndFlush(news);

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

        NewsOpeRecord newsOpeRecord = new NewsOpeRecord();
        newsOpeRecord.setOriginalStatus(NewsStatusEnum.DRAFT.ordinal());
        newsOpeRecord.setResultStatus(NewsStatusEnum.UNDER_REVIEW.ordinal());
        newsOpeRecord.setFeedback(news.getAuthor()+"撰稿:"+news.getTitle());
        newsOpeRecord.setAction("提交审核");
        newsOpeRecord.setOperator(userDetails.getUsername());
        newsOpeRecord.setNews(news);
        newsOpeRecordRepository.saveAndFlush(newsOpeRecord);

        //将状态设置为"审核中"
        news.setStatus(NewsStatusEnum.UNDER_REVIEW.ordinal());
        news.setView(false);
        news.getOpeRecords().add(newsOpeRecord);
        newsRepository.save(news);

        //查找与本账号同地区/镇的具有新闻审核权限的用户
        String queryUid = new String();
        if(userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            queryUid = userDetails.getArea().getUid();
        }else {
            queryUid = userDetails.getTown().getUid();
        }



        //推送消息
        //构造消息
        JSONObject newsMsg = new JSONObject();
        newsMsg.put("subtitle","收到一条待审核新闻");
        newsMsg.put("auditItem",news.getTitle());
        if(userDetails.getTown() == null){
            newsMsg.put("serviceType",userDetails.getArea().getName()+"新闻");
        }else {
            newsMsg.put("serviceType",userDetails.getArea().getName()+" "+userDetails.getTown().getName()+"新闻");
        }
        newsMsg.put("remarkInfo","作者:"+news.getAuthor()+"<点击查看详情>");


        //查找与本账号同地区/镇的具有新闻审核权限的用户
        List<NpcMember> reviewers =  npcMemberRoleService.findByKeyWordAndLevelAndUid(
                NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword(),userDetails.getLevel(),queryUid);
        //向审核人推送消息
        if(!reviewers.isEmpty()){
            for(NpcMember reviewer :reviewers){
                if(reviewer.getAccount() != null){
                    if(reviewer.getAccount().getLoginWeChat() != null){
                        pushMessageService.pushMsg(reviewer.getAccount(), MsgTypeEnum.TO_AUDIT.ordinal(),newsMsg);
                    }
                }
            }
            body.setMessage("成功提交至审核人");
        }else {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("新闻提交成功，但还未设置新闻审核人");
            return body;
        }

        body.setStatus(HttpStatus.OK);
        return body;
    }

    @Override
    public RespBody setPriority(NewsWhereShowDto dto){
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(dto.getUid());
        if (news == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("指定的新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，发布新闻失败",dto.getUid());
            return body;
        }

        news.setWhereShow(dto.getWhereShow());
        newsRepository.saveAndFlush(news);

        body.setMessage("成功修改优先级");
        return body;
    }

    /**
     * 分页查询，非审核人的列表
     * @param
     * @param pageDto 新闻页面dto
     * @return
     */
    @Override
    public RespBody pageForMobile(NewsPageDto pageDto){

        //分页查询条件
        int begin = pageDto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()),
                pageDto.getProperty());

        //用户查询条件
        Specification<News> specification = (root, query, cb)->{
            List<Predicate> predicateList = new ArrayList<>();

            predicateList.add(cb.equal(root.get("level").as(Byte.class), pageDto.getLevel()));

            //按地区编码查询
            if(LevelEnum.AREA.getValue().equals(pageDto.getLevel())){
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class),pageDto.getUid()));
            }else{
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),pageDto.getUid()));
            }

            //按栏目查询
            if (StringUtils.isNotEmpty(pageDto.getNewsType())) {
                predicateList.add(cb.equal(root.get("newsType").get("uid").as(String.class),  pageDto.getNewsType()));
            }

            //按新闻标题模糊查询
            if (StringUtils.isNotEmpty(pageDto.getTitle())) {
                predicateList.add(cb.like(root.get("title").as(String.class), "%" + pageDto.getTitle() + "%"));
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
     * 分页查询，审核人的列表
     * @param
     * @param dto 新闻页面dto
     * @return
     */
    @Override
    public RespBody mobileReviewPage(UserDetailsImpl userDetails, NewsPageDto dto){
        RespBody<PageVo<NewsPageVo>> body = new RespBody<>();
        //暂时不允许审核人查询查稿状态的新闻
        if (dto.getStatus() !=null){
            if(dto.getStatus() == NewsStatusEnum.CREATED.ordinal() || dto.getStatus() == NewsStatusEnum.DRAFT.ordinal()) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("您不能查询草稿状态新闻");
                return body;
            }
        }else {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("状态值不能为空");
            return body;
        }

        int begin = dto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(),currentAccount.getNpcMembers());

        if (npcMember != null) {
            List<String> roleKeywords = npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList());

            //如果是新闻审核人
            if (roleKeywords.contains(NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword()) ) {

                //用户查询条件
                Specification<News> specification = (root, query, cb)->{
                    List<Predicate> predicateList = new ArrayList<>();

                    predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                    predicateList.add(cb.equal(root.get("level").as(Byte.class), dto.getLevel()));

                    if(userDetails.getTown() != null){
                        if(dto.getLevel().equals(LevelEnum.TOWN.getValue())){
                            predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
                        }
                    }

                    //按新闻状态查询
                    if (dto.getStatus() != -1) {
                        predicateList.add(cb.equal(root.get("status").as(Integer.class), dto.getStatus()));
                    }else {
                        predicateList.add(cb.notEqual(root.get("status").as(Integer.class), NewsStatusEnum.CREATED.ordinal()));
                        predicateList.add(cb.notEqual(root.get("status").as(Integer.class), NewsStatusEnum.DRAFT.ordinal()));
                    }

                    return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
                };

                //查询数据库
                Page<News> page = newsRepository.findAll(specification,pageable);

                //封装查询结果
                PageVo<NewsPageVo> pageVo = new PageVo<>(page, dto);
                pageVo.setContent(page.getContent().stream().map(NewsPageVo::convert).collect(Collectors.toList()));

                //返回数据
                body.setData(pageVo);
            }else {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("您暂无此权限");
                return body;
            }
        }
        return body;
    }

    /**
     * 审核人获取新闻详情，主要不同的是，会获取到操作记录
     * @param userDetails 用户信息
     * @param uid 新闻审核参数封装对象
     * @param level
     * @return
     */
    @Override
    public RespBody detailsForMobileReviewer(UserDetailsImpl userDetails,String uid,Byte level){
        RespBody<NewsDetailsForMobileVo> body = new RespBody<>();
        if(uid.isEmpty()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("UID不能为空");
            return body;
        }
        News news = newsRepository.findByUid(uid);
        if(news == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在",uid);
            return body;
        }

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(level,currentAccount.getNpcMembers());

        List<String> roleKeywords = npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList());
        if (roleKeywords.contains(NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword()) ) {
            news.setView(true);
        } else {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("您没有新闻审核权限");
            return body;
        }
        newsRepository.saveAndFlush(news);

        NewsDetailsForMobileVo vo = NewsDetailsForMobileVo.convert(news);

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
    public RespBody review(UserDetailsImpl userDetails, NewsReviewDto dto){
        RespBody body = new RespBody();
        News news = newsRepository.findByUid(dto.getUid());
        if (news == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("指定的新闻不存在");
            LOGGER.warn("uid为 {} 的新闻不存在，审核新闻失败",dto.getUid());
            return body;
        }

        if(news.getStatus() != NewsStatusEnum.UNDER_REVIEW.ordinal() && news.getStatus() != NewsStatusEnum.NOT_APPROVED.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("指定的新闻不在[审核中][不通过]状态");
            LOGGER.warn("uid为 {} 的新闻不在[审核中][不通过]状态，审核新闻失败",dto.getUid());
            return body;
        }

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(),currentAccount.getNpcMembers());
        if(npcMember == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("不存在该代表");
            LOGGER.warn("不存在该代表");
            return body;
        }

        //添加操作记录
        NewsOpeRecord newsOpeRecord = new NewsOpeRecord();
        newsOpeRecord.setOriginalStatus(news.getStatus());

        //如果审核结果为:通过
        if(dto.getPass()){
            //将新闻状态设置为"待发布"(可发布)状态
            news.setStatus(NewsStatusEnum.RELEASABLE.ordinal());
            newsOpeRecord.setResultStatus(NewsStatusEnum.RELEASABLE.ordinal());
        }else {
            //如果审核结果为:不通过

            //将新闻状态设置为"不通过"状态
            news.setStatus(NewsStatusEnum.NOT_APPROVED.ordinal());
            newsOpeRecord.setResultStatus(NewsStatusEnum.NOT_APPROVED.ordinal());
        }

        //对新闻的反馈意见
        newsOpeRecord.setFeedback(dto.getFeedback());
        //将调用该接口的当前用户记录为该新闻的审核人(操作者)
        newsOpeRecord.setOperator(npcMember.getName());
        newsOpeRecord.setAction("审核");
        //先查出来再关联，确保不会报瞬态错误
        newsOpeRecord.setNews(newsRepository.findByUid(news.getUid()));
        newsOpeRecordRepository.saveAndFlush(newsOpeRecord);

        news.setView(true);
        news.getOpeRecords().add(newsOpeRecord);
        newsRepository.saveAndFlush(news);

        String queryUid = new String();
        if(dto.getLevel().equals(LevelEnum.AREA.getValue())){
            queryUid = userDetails.getArea().getUid();
        }else {
            queryUid = userDetails.getTown().getUid();
        }

        //查找与本账号同地区/镇的具有新闻审核权限的用户
        List<NpcMember> reviewers =  npcMemberRoleService.findByKeyWordAndLevelAndUid(
                NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword(),dto.getLevel(),queryUid);

        //构造消息
        JSONObject newsMsg = new JSONObject();
        newsMsg.put("subtitle","收到一条新闻的审核结果");
        newsMsg.put("auditItem",news.getTitle());
        newsMsg.put("result",dto.getPass()?"通过(可发布)":"不通过(驳回修改)");
        newsMsg.put("remarkInfo","操作人："+ newsOpeRecord.getOperator()+"<点击查看详情>");

        //给除了自己以外的其他审核人都会推送结果
        for(NpcMember reviewer:reviewers){
            if(reviewer.getAccount() != null){
                if(reviewer.getAccount().getLoginWeChat() != null){
                    if(!reviewer.getAccount().getUid().equals(userDetails.getUid())){
                        pushMessageService.pushMsg(reviewer.getAccount(),MsgTypeEnum.AUDIT_RESULT.ordinal(),newsMsg);
                    }
                }
            }

        }

        body.setMessage("完成新闻审核");
        return body;
    }

    /**
     * 审核人对新闻进行审核
     * @param userDetails 用户信息
     * @param uid
     * @return
     */
    @Override
    public RespBody publishForMobile(UserDetailsImpl userDetails,String uid,Byte level){
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

        //添加操作记录
        NewsOpeRecord newsOpeRecord = new NewsOpeRecord();
        newsOpeRecord.setOriginalStatus(news.getStatus());
        newsOpeRecord.setResultStatus(NewsStatusEnum.RELEASED.ordinal());
        newsOpeRecord.setFeedback("完成发布"+news.getTitle());
        newsOpeRecord.setOpTime(new Date());
        newsOpeRecord.setAction("发布");
        //将调用该接口的当前用户记录为该新闻的(操作者)
        Account currentAccount = accountRepository.findByUid(userDetails.getUid());

        if(currentAccount != null){
            NpcMember operator = NpcMemberUtil.getCurrentIden(level,currentAccount.getNpcMembers());
            //免得有时候报空指针异常
            if(operator != null){
                newsOpeRecord.setOperator(operator.getName());
            }
        }
        newsOpeRecord.setNews(newsRepository.findByUid(news.getUid()));
        newsOpeRecordRepository.saveAndFlush(newsOpeRecord);

        //将状态设置为已发布
        news.setStatus(NewsStatusEnum.RELEASED.ordinal());
        //将新闻设置为公开状态
        news.setPublished(true);
        news.getOpeRecords().add(newsOpeRecord);
        newsRepository.saveAndFlush(news);

        //如果这条新闻是需要推送的，群发消息
        if(news.getPushNews()){
            //构造消息
            JSONObject newsMsg = new JSONObject();
            newsMsg.put("subtitle","收到一条新闻");
            newsMsg.put("time",news.getPublishAt());
            newsMsg.put("theme",news.getTitle());
            newsMsg.put("remarkInfo","来源:"+news.getAuthor()+"<点击查看详情>");

            //查找与本账号相同地区及层级的代表
            List<NpcMember> receivers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),level);
            for(NpcMember receiver:receivers){
                if(receiver.getAccount() != null){
                    if(receiver.getAccount().getLoginWeChat() != null){
                        pushMessageService.pushMsg(receiver.getAccount(),MsgTypeEnum.CONFERENCE.ordinal(),newsMsg);
                    }
                }

            }
        }

        body.setMessage("新闻公开发布成功");
        return body;
    }


}
