package com.cdkhd.npc.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "t_role")
public class Role extends BaseDomain {

    private boolean enabled = true;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String keyword;

    //1选民   2 镇代表  3 县代表
    private Byte identity;

    @ManyToMany(targetEntity = Permission.class)
    @JoinTable(
            name = "m_role_permission",
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "id", nullable = false)}
    )
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(targetEntity = NpcMember.class, mappedBy = "npcMemberRoles")
    private Set<NpcMember> npcMembers = new HashSet<>();

    @ManyToMany(targetEntity = Account.class, mappedBy = "accountRoles")
    private Set<Account> accounts = new HashSet<>();
}
