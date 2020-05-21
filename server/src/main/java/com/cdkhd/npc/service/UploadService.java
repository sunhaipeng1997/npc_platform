package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AccountPageDto;
import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    RespBody upload(UserDetailsImpl userDetails, MultipartFile avatar);

}
