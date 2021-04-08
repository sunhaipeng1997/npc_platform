package com.cdkhd.npc.util;

import org.junit.Test;

public class CodeUtilsTest {
    @Test
    public void testGenerateRecommendCode() {
        for (int i = 0; i < 10; ++i) {
            System.out.println(CodeUtils.generateRecommendCode(i));
        }
    }
}
