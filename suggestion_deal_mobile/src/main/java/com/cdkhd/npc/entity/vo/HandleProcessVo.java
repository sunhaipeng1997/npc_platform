package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.HandleProcess;
import com.cdkhd.npc.entity.UnitImage;
import com.cdkhd.npc.enums.ImageTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Comparator;
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

        List<UnitImage> imageList = new ArrayList<>(hp.getProcessImages());
        imageList.sort(Comparator.comparing(UnitImage::getCreateTime));

        vo.setImages(imageList.stream()
                .filter(unitImage -> unitImage.getType().equals(ImageTypeEnum.HANDLE_PROCESS.getValue()))
                .map(UnitImage::getUrl)
                .collect(Collectors.toList()));

        return vo;
    }
}
