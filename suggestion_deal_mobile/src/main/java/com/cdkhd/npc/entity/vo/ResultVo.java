package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Result;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultVo {
    //办理结果
    private String result;

    //是否接受办理结果
    private Boolean accept;

    //原因
    private String reason;

    //结果附件url
    private List<String> attachments;

    public static ResultVo convert(Result result) {
        return new ResultVo();
    }
}
