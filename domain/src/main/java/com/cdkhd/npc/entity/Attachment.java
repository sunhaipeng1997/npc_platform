package com.cdkhd.npc.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "t_attachment")
public class Attachment extends BaseDomain {

	@ManyToOne(cascade = CascadeType.ALL, targetEntity = Notification.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id")
	private Notification notification;

	@Column(nullable = false, unique = true)
	private String url;

	@Column(name = "file_name")
	private String fileName;

}
