package com.cdkhd.npc.util;

import java.util.Arrays;

public class CodeUtils {
    private static final char[] CHARS = new char[] {'F', 'L', 'G', 'W', '5', 'X', 'C', '3',
            '9', 'Z', 'M', '6', '7', 'Y', 'R', 'T', '2', 'H', 'S', '8', 'D', 'V', 'E', 'J', '4', 'K',
            'Q', 'P', 'U', 'A', 'N', 'B'};
    //推荐码长度
    private static final int CODE_LENGTH = 6;

    //生成推荐码
    public static String generateRecommendCode(long id) {
        //补位，并扩大整体
        id = id * 127 + 54323;
        //将 id 转换成32进制的值
        long[] b = new long[CODE_LENGTH];
        //32进制数
        b[0] = id;
        for (int i = 0; i < CODE_LENGTH - 1; i++) {
            b[i + 1] = b[i] / CHARS.length;
            //扩大每一位的差异
            b[i] = (b[i] + i * b[0]) % CHARS.length;
        }
        b[5] = (b[0] + b[1] + b[2] + b[3] + b[4]) * 127 % CHARS.length;

        //进行混淆
        long[] codeIndexArray = new long[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            codeIndexArray[i] = b[i * 133 % CODE_LENGTH];
        }

        StringBuilder buffer = new StringBuilder();
        Arrays.stream(codeIndexArray).boxed().map(Long::intValue).map(t -> CHARS[t]).forEach(buffer::append);
        return buffer.toString();
    }
}
