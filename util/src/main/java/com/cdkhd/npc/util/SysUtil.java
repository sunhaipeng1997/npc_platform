package com.cdkhd.npc.util;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public abstract class SysUtil {

    public static String uid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前时间的零点
     *
     * @return
     */
    public static Date todayZeroTime() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        return now.getTime();
    }

    /*public static boolean checkParam(Object obj) {
        Class<?> classInfo = obj.getClass();
        Method[] methods = classInfo.getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                try {
                    Object result = method.invoke(obj);
                    if (result == null)
                        return false;
                } catch (Exception e) {
                    System.out.println("验证了非getter方法");
                    e.printStackTrace();
                }
            }
        }
        return true;
    }*/
}
