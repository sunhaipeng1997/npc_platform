package com.cdkhd.npc.entity.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class AttachmentDto {

    private MultipartFile file;
}
