package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.vo.BaseVo;
import jdk.nashorn.internal.objects.annotations.Constructor;
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
public class NodeVo{

    //id
    private String id;

    // 区名称
    private String label;

    public NodeVo() {
    }

    public NodeVo(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public static NodeVo convert(String id, String label) {
        NodeVo vo = new NodeVo();
        vo.setId(id);
        vo.setLabel(label);
        return vo;
    }
}
