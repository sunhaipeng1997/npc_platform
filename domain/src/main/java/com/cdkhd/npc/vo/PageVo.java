package com.cdkhd.npc.vo;

import com.cdkhd.npc.dto.PageDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

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
        // copy page size property and direction
        BeanUtils.copyProperties(dto, this);

        // copy totalElements totalPages
        BeanUtils.copyProperties(dto, this);
        // 当前页拥有的元素个数
        this.setCurrentElements(page.getNumberOfElements());
        this.setTotalElements(page.getTotalElements());
        this.setTotalPages(page.getTotalPages());
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCurrentElements() {
        return currentElements;
    }

    public void setCurrentElements(int currentElements) {
        this.currentElements = currentElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageVo<T> copy(Page page, PageDto dto) {

        // copy page size property and direction
        BeanUtils.copyProperties(dto, this);

        // copy totalElements totalPages
        BeanUtils.copyProperties(dto, this);
        // 当前页拥有的元素个数
        this.setCurrentElements(page.getNumberOfElements());
        this.setTotalElements(page.getTotalElements());
        this.setTotalPages(page.getTotalPages());

        return this;
    }
}
