package com.cdkhd.npc.util;


import javax.servlet.http.HttpServletRequest;

/**
 * @创建人
 * @创建时间 2018/12/11
 * @描述
 */
public class ExcelCode {

    /**
     *
     * @Title: encodeFileName
     * @Description: 导出文件转换文件名称编码
     * @param @param fileNames
     * @param @param request
     * @param @return    设定文件
     * @return String    返回类型
     * @throws
     */
    public static String encodeFileName(String fileNames ,HttpServletRequest request) {
        String codedFilename = null;
        try {
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && -1 != agent.indexOf("MSIE") || null != agent
                && -1 != agent.indexOf("Trident") || null != agent && -1 != agent.indexOf("Edge")) {// ie浏览器及Edge浏览器
                codedFilename = java.net.URLEncoder.encode(fileNames, "UTF-8");
            } else if (null != agent && -1 != agent.indexOf("Mozilla")) {// 火狐,Chrome等浏览器
//                codedFilename = new String(fileNames.getBytes("UTF-8"), "iso-8859-1");
                //不管是什么浏览器，都统一编码为UTF-8
                codedFilename = java.net.URLEncoder.encode(fileNames, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codedFilename;
    }
}
