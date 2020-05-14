package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "attachment")
public class Attachment extends BaseDomain {

	@ManyToOne(targetEntity = Notification.class, fetch = FetchType.LAZY)
	private Notification notification;

	@Column(nullable = false, unique = true)
	private String url;

	@Column(name = "name")
	private String name;

	//对应的结果
	@ManyToOne(targetEntity = Result.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "result", referencedColumnName = "id")
	private Result result;

}
