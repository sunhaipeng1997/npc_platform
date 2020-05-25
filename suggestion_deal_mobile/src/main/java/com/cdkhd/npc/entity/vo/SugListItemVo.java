package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 查询建议列表项Vo
 */

@Getter
@Setter
public class SugListItemVo extends BaseVo {
    //建议标题
    private String title;

    //收到时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date receiveTime;

    //建议类型
    private String typeName;

    //是否未读
    private boolean unread;

    //建议封面图url
    private String coverUrl;

    public SugListItemVo(String uid, String title, Date receiveTime, String typeName, boolean unread, String coverUrl) {
        this.setUid(uid);
        this.title = title;
        this.receiveTime = receiveTime;
        this.typeName = typeName;
        this.unread = unread;
        this.coverUrl = coverUrl;
    }

    public static SugListItemVo convert(ConveyProcess process) {
        Suggestion suggestion = process.getSuggestion();
        boolean unread = process.getView().equals((byte)0);

        //获取建议封面图，当建议有图片时，选取第一张作为封面
        String coverUrl = "";
        if (!suggestion.getSuggestionImages().isEmpty()) {
            List<SuggestionImage> imageList = new ArrayList<>(suggestion.getSuggestionImages());
            imageList.sort(Comparator.comparing(SuggestionImage::getId));
            coverUrl = imageList.get(0).getUrl();
        }

        return new SugListItemVo(process.getUid(), suggestion.getTitle(), process.getConveyTime(),
                suggestion.getSuggestionBusiness().getName(), unread, coverUrl);
    }
}
