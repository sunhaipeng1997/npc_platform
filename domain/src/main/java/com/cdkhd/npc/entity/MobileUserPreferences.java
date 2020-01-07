package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.NewsStyleEnum;
import com.cdkhd.npc.enums.ShortcutActionEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Setter
@Getter
@ToString
@Entity
@Table( name ="mobile_user_preferences" )
public class MobileUserPreferences extends BaseDomain {

   //主界面Tabbar中间位置的快捷操作，初值关闭
   @Column(name = "shortcut_action" )
   private String shortcutAction = ShortcutActionEnum.CLOSE.getName();

   //新闻的展现形式：卡片式指类似于公众号推送的样式;列表式(默认)指常规的单元格列表样式
   @Column(name = "news_style" )
   private String newsStyle = NewsStyleEnum.LIST.getName();

    /**
     * 账号表id
     */
   @OneToOne(targetEntity=Account.class, fetch = FetchType.LAZY)
   @JoinColumn(name = "account", referencedColumnName = "id")
   private Account account;
}
