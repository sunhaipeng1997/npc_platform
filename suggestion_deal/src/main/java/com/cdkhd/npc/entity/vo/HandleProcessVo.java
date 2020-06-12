package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.HandleProcess;
import com.cdkhd.npc.entity.UnitImage;
import com.cdkhd.npc.enums.ImageTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class HandleProcessVo extends BaseVo {
    private String description;

    private List<String> images;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handleTime;


    public static HandleProcessVo convert(HandleProcess hp) {
        HandleProcessVo vo = new HandleProcessVo();
        BeanUtils.copyProperties(hp,vo);
        vo.setImages(hp.getProcessImages().stream().filter(img -> img.getType().equals(ImageTypeEnum.HANDLE_PROCESS.getValue())).map(UnitImage::getUrl).collect(Collectors.toList()));
        return vo;
    }
}
