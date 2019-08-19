package com.vectorjm.dichattinz;

public class ChatBoutImage extends ChatBout {

    private String imageUrl;

    public ChatBoutImage(String id, String uid, Long dateCreated, String text, String imageUrl) {
        super(id, uid, dateCreated, text);
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getType() {
        return "ChatBoutImage";
    }
}
