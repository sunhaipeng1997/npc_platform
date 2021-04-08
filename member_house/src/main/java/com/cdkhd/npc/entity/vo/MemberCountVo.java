package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.CommonVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberCountVo {

    private String uid;

    private String name;

    //统计
    private List<CountVo> count;

}
