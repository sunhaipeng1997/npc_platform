package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="token" )
public class Token extends BaseDomain {


   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "expire_at" )
	private Date expireAt;

   	@Column(name = "sign_at" )
	private Date signAt;

   	@Column(name = "token" )
	private String token;

   	@Column(name = "account_id" )
	private Long accountId;

}
