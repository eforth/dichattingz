package com.vectorjm.dichattinz;

public class ChatBoutPlayable extends ChatBout {

    private String playableUrl;
    private boolean isVideo;

    public ChatBoutPlayable(String id, String uid, Long dateCreated, String text, String playableUrl) {
        super(id, uid, dateCreated, text);
        this.playableUrl = playableUrl;
    }

    public String getPlayableUrl() {
        return playableUrl;
    }

    public void setPlayableUrl(String playableUrl) {
        this.playableUrl = playableUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    @Override
    public String getType() {
        return "ChatBoutPlayable";
    }
}
