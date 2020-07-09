package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Result;
import com.cdkhd.npc.entity.UnitImage;
import com.cdkhd.npc.enums.ImageTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ResultVo {

    //办理结果
    private String result;

    //是否接受办理结果
    private Boolean accepted;

    //原因
    private String reason;

    //结果附件url
    private List<String> images;

    public static ResultVo convert(Result result) {
        if (result == null)
            return null;

        ResultVo vo = new ResultVo();

        BeanUtils.copyProperties(result, vo);

        vo.setImages(result.getResultImages()
                .stream()
                .filter(uImg -> uImg.getType().equals(ImageTypeEnum.HANDLE_RESULT.getValue()))
                .map(UnitImage::getUrl)
                .collect(Collectors.toList()));

        return vo;
    }
}
