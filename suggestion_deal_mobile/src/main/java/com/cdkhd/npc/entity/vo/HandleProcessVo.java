package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.HandleProcess;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class HandleProcessVo extends BaseVo {

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+08")
    private Date handleTime;

    private String description;

    private List<String> images;

    public static HandleProcessVo convert(HandleProcess hp) {
        HandleProcessVo vo = new HandleProcessVo();
        BeanUtils.copyProperties(hp, vo);
        vo.setImages(hp.getProcessImages().stream().map(processImage -> processImage.getUrl()).collect(Collectors.toList()));
        return vo;
    }
}
