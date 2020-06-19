package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.UnitSuggestion;
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

    //时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    //建议类型
    private String typeName;

    //是否未读
    private boolean unread;

    //建议封面图url
    private String coverUrl;

    //政府名称
    private String govName;

    public SugListItemVo(String uid, String title, Date date, String typeName, boolean unread, String coverUrl, String govName) {
        this.setUid(uid);
        this.title = title;
        this.date = date;
        this.typeName = typeName;
        this.unread = unread;
        this.coverUrl = coverUrl;
        this.govName = govName;
    }

    public static SugListItemVo convert(ConveyProcess process) {
        boolean unread = !process.getUnitView();

        Suggestion suggestion = process.getSuggestion();
        String coverUrl = getCoverUrl(suggestion);

        return new SugListItemVo(process.getUid(), suggestion.getTitle(), process.getConveyTime(),
                suggestion.getSuggestionBusiness().getName(), unread, coverUrl, process.getGovernmentUser().getGovernment().getName());
    }

    public static SugListItemVo convert(UnitSuggestion unitSuggestion) {
        boolean unread = !unitSuggestion.getUnitView();

        Suggestion suggestion = unitSuggestion.getSuggestion();
        String coverUrl = getCoverUrl(suggestion);

        return new SugListItemVo(unitSuggestion.getUid(), suggestion.getTitle(), unitSuggestion.getAcceptTime(),
                suggestion.getSuggestionBusiness().getName(), unread, coverUrl, unitSuggestion.getGovernmentUser().getGovernment().getName());
    }

    //获取建议封面图，当建议有图片时，选取第一张作为封面
    private static String getCoverUrl(Suggestion suggestion) {
        if (!suggestion.getSuggestionImages().isEmpty()) {
            List<SuggestionImage> imageList = new ArrayList<>(suggestion.getSuggestionImages());
            imageList.sort(Comparator.comparing(SuggestionImage::getId));
            return imageList.get(0).getUrl();
        }
        return "";
    }
}
