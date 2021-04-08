package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.NewsType;
import com.cdkhd.npc.entity.dto.NewsTypeAddDto;
import com.cdkhd.npc.entity.dto.NewsTypePageDto;
import com.cdkhd.npc.entity.vo.NewsTypeVo;
import com.cdkhd.npc.enums.DirectionEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.NewsRepository;
import com.cdkhd.npc.repository.base.NewsTypeRepository;
import com.cdkhd.npc.service.NewsTypeService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NewsTypeServiceImpl implements NewsTypeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberServiceImpl.class);

    private NewsTypeRepository newsTypeRepository;
    private NewsRepository newsRepository;

    @Autowired
    public NewsTypeServiceImpl(NewsTypeRepository newsTypeRepository, NewsRepository newsRepository) {
        this.newsTypeRepository = newsTypeRepository;
        this.newsRepository = newsRepository;
    }

    /**
     * 添加新闻类别(栏目)
     *
     * @param addDto 待添加的新闻类型信息
     * @return 添加结果
     */
    @Override
    public RespBody addNewsType(UserDetailsImpl userDetails, NewsTypeAddDto addDto) {
        RespBody body = new RespBody();

        NewsType newsType = new NewsType();

        newsType.setArea(userDetails.getArea());
        newsType.setTown(userDetails.getTown());
        newsType.setLevel(userDetails.getLevel());

        Integer maxSequence = newsTypeRepository.findMaxSequence();
        if (maxSequence == null) {
            maxSequence = 0;
        }
        newsType.setSequence(maxSequence + 1);

        newsType.setName(addDto.getName());
        newsType.setRemark(addDto.getRemark());

        newsTypeRepository.saveAndFlush(newsType);

        body.setMessage("添加新闻类别成功");
        return body;
    }

    /**
     * 更新新闻类别(栏目)
     *
     * @param dto 待更新的新闻类型信息
     * @return 更新结果
     */
    @Override
    public RespBody updateNewsType(UserDetailsImpl userDetails, NewsTypeAddDto dto) {
        RespBody body = new RespBody();
        NewsType newsType = newsTypeRepository.findByUid(dto.getUid());
        if (newsType == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻栏目不存在");
            LOGGER.warn("uid为 {} 的新闻栏目不存在，更新新闻类别失败", dto.getUid());
            return body;
        }

        if (!dto.getName().isEmpty()) {
            newsType.setName(dto.getName());
        }

        if (!dto.getRemark().isEmpty()) {
            newsType.setRemark(dto.getRemark());
        }

        if (dto.getStatus() != null) {
            newsType.setStatus(dto.getStatus());
        }

        newsTypeRepository.save(newsType);

        body.setMessage("修改新闻栏目成功");
        return body;
    }

    /**
     * 删除新闻类别(栏目)
     *
     * @param uid 待删除的新闻类别的uid
     * @return 删除结果
     */
    @Override
    public RespBody deleteNewsType(@NotBlank String uid) {
        RespBody body = new RespBody();
        NewsType newsType = newsTypeRepository.findByUid(uid);
        if (newsType == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻类型不存在");
            LOGGER.warn("uid为 {} 的新闻类别不存在,删除新闻类别失败", uid);
            return body;
        }

        int number = newsRepository.countByNewsType(newsType);
        if (number > 0) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("存在该类别新闻，不能删除该类别");
            LOGGER.warn("存在{}的新闻,删除新闻类别失败", newsType.getName());
            return body;
        }

        newsTypeRepository.delete(newsType);

        body.setMessage("删除新闻类别成功");
        return body;
    }

    /**
     * 分页查询新闻类别(栏目)
     *
     * @param pageDto 查询条件
     * @return 查询结果
     */
    @Override
    public RespBody pageOfNewsType(UserDetailsImpl userDetails, NewsTypePageDto pageDto) {

        //分页查询条件
        int begin = pageDto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()),
                pageDto.getProperty());

        //用户查询条件
        Specification<NewsType> specification = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            predicateList.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            if (userDetails.getTown() != null) {
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }

            //按类别名称模糊查询
            if (StringUtils.isNotEmpty(pageDto.getName())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + pageDto.getName() + "%"));
            }

            //按状态查询
            if (pageDto.getStatus() != null) {
                predicateList.add(cb.equal(root.get("status").as(Byte.class), pageDto.getStatus()));
            }

            return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };

        //查询数据库
        Page<NewsType> page = newsTypeRepository.findAll(specification, pageable);

        //封装查询结果
        PageVo<NewsTypeVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(page.getContent().stream().map(NewsTypeVo::convert).collect(Collectors.toList()));

        //返回数据
        RespBody<PageVo> body = new RespBody<>();
        body.setData(pageVo);

        return body;
    }

    /**
     * 分页查询新闻类别(栏目)
     *
     * @param pageDto 查询条件
     * @return 查询结果
     */
    @Override
    public RespBody pageOfNewsTypeForMobile(NewsTypePageDto pageDto) {

        //分页查询条件
        int begin = pageDto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()),
                pageDto.getProperty());

        //用户查询条件
        Specification<NewsType> specification = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();


            predicateList.add(cb.equal(root.get("level").as(Byte.class), pageDto.getLevel()));

            if (LevelEnum.AREA.getValue().equals(pageDto.getLevel())) {
                //按地区编码查询
                predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), pageDto.getUid()));
            } else {
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), pageDto.getUid()));
            }

//            按类别名称模糊查询
            if (StringUtils.isNotEmpty(pageDto.getName())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + pageDto.getName() + "%"));
            }

            //按状态查询,这个必须要
            if (pageDto.getStatus() != null) {
                predicateList.add(cb.equal(root.get("status").as(Byte.class), pageDto.getStatus()));
            }

            return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };

        //查询数据库
        Page<NewsType> page = newsTypeRepository.findAll(specification, pageable);

        //封装查询结果
        PageVo<NewsTypeVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(page.getContent().stream().map(NewsTypeVo::convert).collect(Collectors.toList()));

        //返回数据
        RespBody<PageVo> body = new RespBody<>();
        body.setData(pageVo);

        return body;
    }

    /**
     * 调整新闻类别(栏目)顺序
     *
     * @param uid       调整对象
     * @param direction 移动方向
     * @return 查询结果
     */
    @Override
    public RespBody changeTypeSequence(String uid, int direction) {
        RespBody body = new RespBody();

        NewsType newsType = newsTypeRepository.findByUid(uid);
        if (newsType == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻类型不存在");
            LOGGER.warn("uid为 {} 的新闻类别不存在,调整新闻类别排序失败", uid);
            return body;
        }

        NewsType targetNewsType;
        //上移
        if (direction == DirectionEnum.UP.ordinal()) {
            Sort sort = new Sort(Sort.Direction.DESC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            targetNewsType = newsTypeRepository.findBySequenceDesc(newsType.getSequence(), page).getContent().get(0);

        } else {//下移
            Sort sort = new Sort(Sort.Direction.ASC, "sequence");
            Pageable page = PageRequest.of(0, 1, sort);
            targetNewsType = newsTypeRepository.findBySequenceAsc(newsType.getSequence(), page).getContent().get(0);
        }

        List<NewsType> newsTypeList = this.changeSequence(newsType, targetNewsType);
        newsTypeRepository.saveAll(newsTypeList);

        body.setMessage("调整顺序成功");
        return body;
    }

    /**
     * 交换新闻类型的顺序
     *
     * @param newsType
     * @param targetNewsType
     * @return
     */
    private List<NewsType> changeSequence(NewsType newsType, NewsType targetNewsType) {
        List<NewsType> newsTypeList = Lists.newArrayList();

        //旧顺序
        Integer oldSec = newsType.getSequence();

        //交换顺序
        newsType.setSequence(targetNewsType.getSequence());
        targetNewsType.setSequence(oldSec);

        newsTypeList.add(newsType);
        newsTypeList.add(targetNewsType);

        return newsTypeList;
    }
}
