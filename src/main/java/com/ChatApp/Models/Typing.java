package com.ChatApp.Models;

public class Typing {

	
	 private Long senderId;
	    private Long reciverId;
	    private boolean typing;

	    public Long getSenderId() {
	        return senderId;
	    }

	    public void setSenderId(Long senderId) {
	        this.senderId = senderId;
	    }

	    public Long getReciverId() {
	        return reciverId;
	    }

	    public void setReciverId(Long reciverId) {
	        this.reciverId = reciverId;
	    }

	    public boolean isTyping() {
	        return typing;
	    }

	    public void setTyping(boolean typing) {
	        this.typing = typing;
	    }
}
