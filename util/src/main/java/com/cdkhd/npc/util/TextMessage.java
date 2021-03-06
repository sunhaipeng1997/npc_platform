package com.cdkhd.npc.util;

/**
 * @创建人 lizi
 * @创建时间 2018/12/20
 * @描述 公众号自动回复消息实体类
 */
public class TextMessage {

    private String FromUserName;

    private String ToUserName;

    private String MsgType;

    private long CreateTime;

    private String Content;

    public String getFromUserName() {
        return FromUserName;
    }

    public void setFromUserName(String fromUserName) {
        FromUserName = fromUserName;
    }

    public String getToUserName() {
        return ToUserName;
    }

    public void setToUserName(String toUserName) {
        ToUserName = toUserName;
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setMsgType(String msgType) {
        MsgType = msgType;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(long createTime) {
        CreateTime = createTime;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }
}
