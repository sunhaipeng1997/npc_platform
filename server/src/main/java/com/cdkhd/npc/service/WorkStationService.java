package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.UploadPicDto;
import com.cdkhd.npc.entity.dto.WorkStationAddDto;
import com.cdkhd.npc.entity.dto.WorkStationPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface WorkStationService {

    RespBody page(UserDetailsImpl userDetails, WorkStationPageDto workStationPageDto);

    RespBody upload(UploadPicDto uploadPicDto);

    RespBody addOrUpdate(UserDetailsImpl userDetails, WorkStationAddDto workStationAddDto);

    RespBody changeStatus(String uid);

    RespBody delete(String uid);
}
