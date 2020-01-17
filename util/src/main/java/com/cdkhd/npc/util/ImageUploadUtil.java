package com.cdkhd.npc.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageUploadUtil {

    public static String uid() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
    * 将上传的图片保存到项目指定目录下，并返回图片的url
    * @param kind:图片的种类（履职图片/意见图片/建议/新闻图片等）
    * @param image:上传的图片文件
    * @param width 需要压缩的尺寸宽度
    * @param height
    * return:返回保存的图片的访问url，若保存失败，则返回error
    */
    public static String saveImage(String kind, MultipartFile image,Integer width, Integer height) {
        //得到源文件扩展名
        String orgName = image.getOriginalFilename();
        String extName = FilenameUtils.getExtension(orgName);

        //生成新的文件名
        String filename = String.format("%s.%s", uid(), extName);

        //生成新文件的父目录路径
        String parentPath;
        parentPath = String.format("static/public/%s", kind);
        File imageFile = new File(parentPath, filename);
        File parentFile = imageFile.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                return "error";
            }
        }

        try (InputStream is = image.getInputStream()) {
            // 拷贝文件
//            FileUtils.copyInputStreamToFile(is, imageFile);
            if(width == 0 || height == 0){
                //照片，裁剪尺寸width*height，dpi降为原来的一半
                Thumbnails.of(is).size(500, 300).outputQuality(0.5f).outputFormat("jpg").toFile(imageFile);
            }else {
                //照片，裁剪尺寸width*height，dpi降为原来的一半
                Thumbnails.of(is).size(width, height).outputQuality(0.5f).outputFormat("jpg").toFile(imageFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }

        return "/public/" + kind + "/" + filename;
    }

    /**
     * 将上传的图片保存到项目指定目录下，并返回图片的url
     * 无尺寸参数时，将保持原图尺寸(也可以改进下，等比例减小尺寸)，只是降低质量
     * @param kind:图片的种类（履职图片/意见图片/建议/新闻图片等）
     * @param image:上传的图片文件
     * return:返回保存的图片的访问url，若保存失败，则返回error
     */
    public static String saveImage(String kind, MultipartFile image) {
        //得到源文件扩展名
        String orgName = image.getOriginalFilename();
        String extName = FilenameUtils.getExtension(orgName);

        //生成新的文件名
        String filename = String.format("%s.%s", uid(), extName);

        //生成新文件的父目录路径
        String parentPath;
        parentPath = String.format("static/public/%s", kind);
        File imageFile = new File(parentPath, filename);
        File parentFile = imageFile.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                return "error";
            }
        }

        try (InputStream is = image.getInputStream()) {
            // 拷贝文件
//            FileUtils.copyInputStreamToFile(is, imageFile);

            //照片，裁剪尺寸width*height，dpi降为原来的一半
            Thumbnails.of(is).outputQuality(0.5f).outputFormat("jpg").toFile(imageFile);

        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }

        return "/public/" + kind + "/" + filename;
    }
}
