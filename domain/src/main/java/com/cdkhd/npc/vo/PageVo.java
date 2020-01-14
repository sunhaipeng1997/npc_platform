package com.cdkhd.npc.vo;

import com.cdkhd.npc.dto.PageDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PageVo<T> {

    /**
     * 当前页
     */
    private int page;

    /**
     * 分页大小
     */
    private int size;

    /**
     * 当前页拥有的元素
     */
    private int currentElements;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 总的条数
     */
    private long totalElements;

    /**
     * 排序的属性
     */
    private String property;

    /**
     * 排序的方向
     */
    private String direction;

    /**
     * 查询的结果数据
     */
    private List<T> content = new ArrayList<>();

    public PageVo() {

    }

    public PageVo(Page page, PageDto dto) {
        BeanUtils.copyProperties(dto, this);

        this.setCurrentElements(page.getNumberOfElements());
        this.setTotalElements(page.getTotalElements());
        this.setTotalPages(page.getTotalPages());
    }

    public PageVo(PageDto dto) {
        BeanUtils.copyProperties(dto, this);
    }

    public PageVo<T> copy(Page page, PageDto dto) {
        BeanUtils.copyProperties(dto, this);

        this.setCurrentElements(page.getNumberOfElements());
        this.setTotalElements(page.getTotalElements());
        this.setTotalPages(page.getTotalPages());

        return this;
    }

    public void copy(Page page) {
        // 当前页拥有的元素个数
        this.setCurrentElements(page.getNumberOfElements());
        this.setTotalElements(page.getTotalElements());
        this.setTotalPages(page.getTotalPages());
    }
}
