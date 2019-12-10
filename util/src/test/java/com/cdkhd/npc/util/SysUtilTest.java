package com.cdkhd.npc.util;

import org.junit.Test;

public class SysUtilTest {

    //生成uid测试
    @Test
    public void testUid() {
        for (int i = 0; i < 10; i++) {
            System.out.println(SysUtil.uid());
        }
    }
}
