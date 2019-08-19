package com.vectorjm.dichattinz;

public class ChatBout {

    private String id;
    private String uid;
    private Long dateCreated;
    private String text;

    public ChatBout(String id, String uid, Long dateCreated, String text) {
        this.id = id;
        this.uid = uid;
        this.dateCreated = dateCreated;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return "ChatBout";
    }
}
