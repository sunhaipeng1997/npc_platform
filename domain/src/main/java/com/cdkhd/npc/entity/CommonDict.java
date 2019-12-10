package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @描述 公共码表
 */
@Entity
@Table(name = "common_dict")
public class CommonDict extends BaseDomain{

    //码表code
    @Column(nullable = false)
    private String dictCode;

    //码表名称
    @Column(nullable = false)
    private String dictName;

    //码表类型code
    @Column(nullable = false)
    private String dictKey;

    //码表类型名称
    @Column(nullable = false)
    private String dictKeyName;

    //码表是否删除
    @Column(nullable = false)
    private Boolean isDel = false;

    //码表是否可维护
    @Column(nullable = false)
    private Boolean isMaintain = false;

    //备注
    private String remark;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDictKey() {
        return dictKey;
    }

    public void setDictKey(String dictKey) {
        this.dictKey = dictKey;
    }

    public String getDictKeyName() {
        return dictKeyName;
    }

    public void setDictKeyName(String dictKeyName) {
        this.dictKeyName = dictKeyName;
    }

    public Boolean getIsDel() {
        return isDel;
    }

    public void setIsDel(Boolean isDel) {
        this.isDel = isDel;
    }

    public Boolean getIsMaintain() {
        return isMaintain;
    }

    public void setIsMaintain(Boolean isMaintain) {
        this.isMaintain = isMaintain;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
