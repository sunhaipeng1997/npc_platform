package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @创建人 LiYang
 * @创建时间 2020/05/18
 * @描述 小程序查看我的建议列表分状态查询条件
 */
@Setter
@Getter
public class SugPageDto extends PageDto {

    //建议状态 1：草稿  2：已提交  3：审核失败  4：已办完  5：已办结  0：全部
    private Byte status;

    //代表当前身份 1镇 2区
    private Byte level;

}
