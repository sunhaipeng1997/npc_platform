package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Study;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class StudyVo extends BaseVo {

    //学习资料名字
	private String name;

    //备注
    private String remark;

    //资料路径
    private String url;

    //排序号
    private Integer sequence;

    //学习类型名称
    private String type;
    private String typeName;

    //状态
    private Byte status;
    private String statusName;

    public static StudyVo convert(Study study) {
        StudyVo vo = new StudyVo();
        BeanUtils.copyProperties(study, vo);
        vo.setStatus(study.getStatus());
        vo.setStatusName(StatusEnum.getName(study.getStatus()));
        vo.setType(study.getStudyType().getUid());
        vo.setTypeName(study.getStudyType().getName());
        return vo;
    }
}
