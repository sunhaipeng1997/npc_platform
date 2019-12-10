package com.cdkhd.npc.entity;

import org.junit.Test;

import java.util.HashSet;

public class SetTest {
    @Test
    public void testRoleSet() {
        Set<Role> roles = new HashSet<>();

        Role role1 = new Role();
        role1.setId(1L);
        role1.setUid("1");
        role1.setEnabled(true);
        role1.setKeyword("ADMIN");
        role1.setName("admin");

        Role role2 = new Role();
        role2.setId(1L);
        role2.setUid("1");
        role2.setEnabled(true);
        role2.setKeyword("ADMIN");
        role2.setName("admin");

        roles.add(role1);
        roles.add(role1);
        roles.add(role2);
        System.out.println(roles);
    }
}
