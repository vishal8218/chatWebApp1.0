package com.ChatApp.Models;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class LoginUser {
	@Override
	public String toString() {
		return "LoginUser [userEmailId=" + userEmailId + ", password=" + password + "]";
	}

	    public LoginUser() {} // <-- Jackson needs this

    @NotBlank
	private String userEmailId;
    
    @Min(4)
	private String password;
	public String getUserEmailId() {
		return userEmailId;
	}
	public void setUserEmailId(String userEmailId) {
		this.userEmailId = userEmailId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public LoginUser(String userEmailId, String password) {
	
		this.userEmailId = userEmailId;
		this.password = password;
	}
	

}
