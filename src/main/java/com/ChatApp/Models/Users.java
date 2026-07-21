package com.ChatApp.Models;

public class Users {
	private String email;
	private double amount;
	private int rank;
	private int correctQuestion;
	private String userId;
	private String cid;
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getCorrectQuestion() {
		return correctQuestion;
	}
	public void setCorrectQuestion(int correctQuestion) {
		this.correctQuestion = correctQuestion;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	

}
