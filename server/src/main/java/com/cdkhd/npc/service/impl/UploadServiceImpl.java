package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.PasswordDto;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.entity.vo.MenuVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.AuthService;
import com.cdkhd.npc.service.UploadService;
import com.cdkhd.npc.util.BDSmsUtils;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.util.JwtUtils;
import com.cdkhd.npc.utils.WXAppletUserInfo;
import com.cdkhd.npc.vo.RespBody;
import com.cdkhd.npc.vo.TokenVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Transactional
public class UploadServiceImpl implements UploadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Override
    public RespBody upload(UserDetailsImpl userDetails, MultipartFile avatar) {
        RespBody<String> body = new RespBody<>();
        if (avatar == null){
            body.setMessage("图片上传失败！请稍后重试");
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error("图片上传保存失败");
            return body;
        }
        //保存代表头像至文件系统
        String url = ImageUploadUtil.saveImage("work_station_avatar", avatar,150, 200);
        if (url.equals("error")) {
            body.setMessage("图片上传失败！请稍后重试");
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error("图片上传保存失败");
            return body;
        }
        body.setMessage("图片上传成功");
        body.setData(url);
        return body;
    }

    @Override
    public RespBody uploadPic(UserDetailsImpl userDetails, MultipartFile avatar) {
        RespBody<String> body = new RespBody<>();
        if (avatar == null){
            body.setMessage("图片上传失败！请稍后重试");
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error("图片上传保存失败");
            return body;
        }
        //保存代表头像至文件系统
        String url = ImageUploadUtil.saveImage("work_station_avatar", avatar,400, 300);
        if (url.equals("error")) {
            body.setMessage("图片上传失败！请稍后重试");
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error("图片上传保存失败");
            return body;
        }
        body.setMessage("图片上传成功");
        body.setData(url);
        return body;
    }
}
