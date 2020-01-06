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
    * kind:图片的种类（履职图片/意见图片/建议图片等）
    * uid:图片所属记录（履职信息/意见/建议）的uid
    * image:上传的图片文件
    * return:返回保存的图片到的访问url，若保存失败，则返回error
    */
    public static String saveImage(String kind, String uid, MultipartFile image) {
        String orgName = image.getOriginalFilename();
        String extName = FilenameUtils.getExtension(orgName);
        // 生成新的文件名
        String filename = String.format("%s.%s", uid(), extName);
        String parentPath = "static/public/" + kind + "/" + uid;
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
            //照片，裁剪尺寸500*500，dpi降为原来的一半
            Thumbnails.of(is).size(500, 500).outputQuality(0.5f).outputFormat("jpg").toFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }

        return "/public/" + kind + "/" + uid + "/" + filename;
    }
}