package com.cdkhd.npc.dto;

public class PageDto {

    /**
     * 页码
     */
    private int page = 1;

    /**
     * 分页大小
     */
    private int size = 10;

    /**
     * 排序属性
     */
    private String property = "id";

    /**
     * 排序方向
     */
    private String direction = "DESC";

    public PageDto() {

    }

    public PageDto(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageDto(int page, int size, String property, String direction) {
        this.page = page;
        this.size = size;
        this.property = property;
        this.direction = direction;
    }

    public int getPage() {
        return page;
    }

    public int getPageRequestPage() {
        return page - 1;
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
        this.direction = "ASC".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    }

}
