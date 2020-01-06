package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.NewsType;
import com.cdkhd.npc.entity.dto.NewsTypeAddDto;
import com.cdkhd.npc.entity.dto.NewsTypePageDto;
import com.cdkhd.npc.entity.vo.NewsTypeVo;
import com.cdkhd.npc.repository.base.NewsRepository;
import com.cdkhd.npc.repository.base.NewsTypeRepository;
import com.cdkhd.npc.service.NewsTypeService;
import com.cdkhd.npc.util.SysUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
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

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
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
     * @param addDto 待添加的新闻类型信息
     * @return 添加结果
     */
    @Override
    public RespBody addNewsType(NewsTypeAddDto addDto) {
        RespBody body = new RespBody();

        NewsType newsType = new NewsType();
        BeanUtils.copyProperties(addDto,newsType);
        newsType.setUid(SysUtil.uid());

        newsTypeRepository.saveAndFlush(newsType);

        body.setMessage("添加新闻类别成功");
        return body;
    }

    /**
     * 更新新闻类别(栏目)
     * @param dto 待更新的新闻类型信息
     * @return 更新结果
     */
    @Override
    public RespBody updateNewsType(NewsTypeAddDto dto) {
        RespBody body = new RespBody();
        NewsType newsType = newsTypeRepository.findByUid(dto.getUid());
        if(newsType == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻类型不存在");
            LOGGER.warn("uid为 {} 的新闻类别不存在，更新新闻类别失败",dto.getUid());
            return body;
        }

        BeanUtils.copyProperties(dto,newsType);

        newsTypeRepository.save(newsType);

        body.setMessage("修改新闻类别成功");
        return body;
    }


    /**
     * 删除新闻类别(栏目)
     * @param uid 待删除的新闻类别的uid
     * @return 删除结果
     */
    @Override
    public RespBody deleteNewsType(String uid) {
        RespBody body = new RespBody();
        NewsType newsType = newsTypeRepository.findByUid(uid);
        if(newsType == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该新闻类型不存在");
            LOGGER.warn("uid为 {} 的新闻类别不存在,删除新闻类别失败",uid);
            return body;
        }

        int number = newsRepository.countByNewsType(newsType);
        if(number > 0){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("存在该类别新闻，不能删除该类别");
            LOGGER.warn("存在{}的新闻,删除新闻类别失败",newsType.getName());
            return body;
        }

        newsTypeRepository.delete(newsType);

        body.setMessage("删除新闻类别成功");
        return body;
    }


    /**
     * 分页查询新闻类别(栏目)
     * @param pageDto 查询条件
     * @return 查询结果
     */
    @Override
    public RespBody pageOfNewsType(NewsTypePageDto pageDto) {

        //分页查询条件
        int begin = pageDto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()),
                pageDto.getProperty());

        //用户查询条件
        Specification<NewsType> specification = (root, query, cb)->{
            List<Predicate> predicateList = new ArrayList<>();

            //按类别名称模糊查询
            if (StringUtils.isNotEmpty(pageDto.getName())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + pageDto.getName() + "%"));
            }

            //按镇/社区名称模糊查询
            if (StringUtils.isNotEmpty(pageDto.getTown())) {
                predicateList.add(cb.like(root.get("town").as(String.class), "%" + pageDto.getTown() + "%"));
            }

            //按状态查询
            if (pageDto.getStatus() != null) {
                predicateList.add(cb.equal(root.get("status").as(Byte.class), pageDto.getStatus()));
            }

            //按地区编码查询
            if (pageDto.getArea() != null) {
                predicateList.add(cb.equal(root.get("area").as(Integer.class), pageDto.getArea()));
            }

            return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };

        //查询数据库
        Page<NewsType> page = newsTypeRepository.findAll(specification,pageable);

        //封装查询结果
        PageVo<NewsTypeVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(page.getContent().stream().map(NewsTypeVo::convert).collect(Collectors.toList()));

        //返回数据
        RespBody<PageVo> body = new RespBody<>();
        body.setData(pageVo);

        return body;
    }
}
