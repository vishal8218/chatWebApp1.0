package com.ChatApp.Models;

public class MessageContent {
	
	@Override
	public String toString() {
		return "MessageContent [reciverId=" + reciverId + ", senderId=" + senderId + ", messageContent="
				+ messageContent + ", messageType=" + messageType + ", time=" + time + ", date=" + date + ", isRead="
				+ isRead + "]";
	}
	private String reciverId,senderId;
	private String messageId;
	
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getReciverId() {
		return reciverId;
	}
	public void setReciverId(String reciverId) {
		this.reciverId = reciverId;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	private String messageContent;
	private String messageType;
	private String time,date;
	private boolean isRead ;
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
		
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
	
	

}
