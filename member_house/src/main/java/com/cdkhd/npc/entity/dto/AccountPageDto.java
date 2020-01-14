package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  ly
 * @Date 2019-01-10
 */

@Setter
@Getter
public class AccountPageDto extends PageDto {

   	//真实姓名
	private String realname;

   	//手机号
    private String mobile;

}
