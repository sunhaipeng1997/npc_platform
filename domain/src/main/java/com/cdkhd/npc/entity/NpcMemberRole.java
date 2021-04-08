package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@Entity
@Table ( name ="npc_member_role" )
public class NpcMemberRole extends BaseDomain {

   	@Column
	private String keyword;

   	//是否可用
   	@Column
	private Byte status = StatusEnum.ENABLED.getValue();

	//代表身份角色细分 （必须） 1、普通代表  2、人大主席  3、特殊人员 ，（非必须）4、新闻审核人  5、通知公告审核人  6、 建议接受人 7、履职小组审核人 8、 履职总审核人
   	@Column
	private String name;

   	//是否是必须的角色
   	private Boolean isMust;

   	//是否特殊身份
   	private Boolean special;

	@ManyToMany
	@JoinTable(
			name = "npc_member_role_permission_mid",
			joinColumns = {
					@JoinColumn(name = "npc_member_role_id", referencedColumnName = "id")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "permission_id", referencedColumnName = "id")
			}
	)
   	private Set<Permission> permissions;


    @ManyToMany
    @JoinTable(
            name = "npc_member_role_mid",
            joinColumns = {
                    @JoinColumn(name = "npc_member_role_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "npc_member_id", referencedColumnName = "id")
            }
    )
    private Set<NpcMember> npcMembers;
}
