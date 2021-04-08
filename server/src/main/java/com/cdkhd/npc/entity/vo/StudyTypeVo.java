package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.StudyType;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class StudyTypeVo extends BaseVo {

    //学习类型名称
	private String name;

    //排序号
    private Integer sequence;

    //备注
    private String remark;

    //状态
    private Byte status;
    private String statusName;

    private List<StudyVo> studyVos;

    public static StudyTypeVo convert(StudyType studyType) {
        StudyTypeVo vo = new StudyTypeVo();
        BeanUtils.copyProperties(studyType, vo);
        vo.setStatusName(StatusEnum.getName(studyType.getStatus()));
        vo.setStudyVos(studyType.getStudies().stream()
                .filter(study -> study.getStatus().equals(StatusEnum.ENABLED.getValue()))
                .map(StudyVo::convert)
                .sorted(Comparator.comparing(StudyVo::getSequence))
                .collect(Collectors.toList()));
        return vo;
    }
}
