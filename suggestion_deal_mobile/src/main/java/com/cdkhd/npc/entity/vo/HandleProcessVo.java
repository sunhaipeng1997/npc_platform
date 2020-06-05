package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.HandleProcess;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Getter
@Setter
public class HandleProcessVo extends BaseVo {
    private String description;

    private List<String> images;

    public static HandleProcessVo convert(HandleProcess hp) {
        HandleProcessVo vo = new HandleProcessVo();

        BeanUtils.copyProperties(hp, vo);
        return vo;
    }
}
