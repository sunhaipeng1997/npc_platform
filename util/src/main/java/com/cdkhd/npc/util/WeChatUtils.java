package com.cdkhd.npc.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class WeChatUtils {

    /**
     * 验证微信服务器的身份。将timestamp nonce token字典排序后进行sha-1加密，将加密结果与signature比对
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @param token
     * @return
     */
    public static boolean checkSign(String signature, String timestamp, String nonce, String token) {
        String[] arr = new String[] {token, timestamp, nonce};

        //字典排序
        Arrays.sort(arr);

        //进行SHA-1 hash
        String toHash = arr[0] + arr[1] + arr[2];
        String ret = null;
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(toHash.getBytes(StandardCharsets.UTF_8));
            ret = byteToHex(crypt.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return ret.equals(signature);
    }

    /**
     * 生成config参数，用于前端调用js api
     * @param jsApiTicket jsapi_ticket
     * @param url 调用js api接口的页面url
     * @return config参数
     */
    public static Map<String, String> sign(String jsApiTicket, String url) {
        Map<String, String> ret = new HashMap<>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsApiTicket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;

        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes(StandardCharsets.UTF_8));
            signature = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsApiTicket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    /**
     * 将字节转换为16进制字符串
     * @param hash
     * @return
     */
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
