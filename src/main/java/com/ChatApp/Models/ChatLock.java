package com.ChatApp.Models;

public class ChatLock {

    private String senderId;
    private String receiverId;
    private boolean isLock;
    private String password;

    public ChatLock() {
    }


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public boolean getIsLock() {
        return isLock;
    }

    public void setIsLock(boolean lock) {
        isLock = lock;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ChatLock{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", isLock=" + isLock +
                ", password='[PROTECTED]'" +
                '}';
    }
}


