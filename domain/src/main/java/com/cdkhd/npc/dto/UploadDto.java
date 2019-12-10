package com.cdkhd.npc.dto;

import org.springframework.web.multipart.MultipartFile;

public class UploadDto {

    //展示文件名称
    private String fileName;

    //文件
    private MultipartFile file;

    //类型
    private String type;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
