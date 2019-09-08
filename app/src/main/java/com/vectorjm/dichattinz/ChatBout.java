package com.vectorjm.dichattinz;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ChatBout {

    private String id;
    private String uid;
    private Long dateCreated;
    private String text;

    public ChatBout() {
    }

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

    public static ChatBout toChatBout(QueryDocumentSnapshot document) {

        ChatBout chatBout = new ChatBout();

        if (document == null) return null;

        chatBout.id = document.getId();
        chatBout.uid = document.getData().get("uid").toString();
        chatBout.dateCreated = Long.parseLong(document.getData().get("dateCreated").toString());
        chatBout.text = document.getData().get("text").toString();

        return chatBout;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("uid", uid);
        map.put("dateCreated", dateCreated);
        map.put("text", text);
        map.put("type", getType());

        return map;
    }
}
