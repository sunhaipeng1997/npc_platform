package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class EdgeVo {

    // 资源
    private String source;

    // 指向
    private String target;

    public static EdgeVo convert(String source, String target) {
        EdgeVo vo = new EdgeVo();
        vo.setSource(source);
        vo.setTarget(target);
        return vo;
    }
}
