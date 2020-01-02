package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.StatusEnum;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SetTest {
    @Test
    public void testRoleSet() {
        Set<AccountRole> roles = new HashSet<>();

        AccountRole accountRole1 = new AccountRole();
        accountRole1.setId(1L);
        accountRole1.setUid("1");
        accountRole1.setStatus(StatusEnum.ENABLED.getValue());
        accountRole1.setName(AccountRoleEnum.BACKGROUND_ADMIN.getName());


        AccountRole accountRole2 = new AccountRole();
        accountRole2.setId(1L);
        accountRole2.setUid("1");
        accountRole1.setStatus(StatusEnum.ENABLED.getValue());
        accountRole1.setName(AccountRoleEnum.BACKGROUND_ADMIN.getName());

        roles.add(accountRole1);
        roles.add(accountRole1);
        roles.add(accountRole2);
        System.out.println(roles);
    }
}
