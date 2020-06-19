package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.PasswordDto;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.service.UploadService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/manager/upload")
public class UploadController {
    private UploadService uploadService;

    @Autowired
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * 上传头像
     * */
    @PostMapping("/uploadAvatar")
    public ResponseEntity upload(@CurrentUser UserDetailsImpl userDetails, MultipartFile file){
        RespBody body = uploadService.upload(userDetails, file);
        return ResponseEntity.ok(body);
    }

    /**
     * 上传其他图片  400 * 300
     * */
    @PostMapping("/uploadPic")
    public ResponseEntity uploadPic(@CurrentUser UserDetailsImpl userDetails, MultipartFile file){
        RespBody body = uploadService.uploadPic(userDetails, file);
        return ResponseEntity.ok(body);
    }

}
