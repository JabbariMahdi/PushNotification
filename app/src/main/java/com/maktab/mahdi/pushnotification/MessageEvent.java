package com.maktab.mahdi.pushnotification;

public class MessageEvent {

    /*public final MediaPlayer player;*/
    private final String senderName;
    private final String message;

    public MessageEvent(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }
}
