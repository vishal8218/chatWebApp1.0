package com.ChatApp.Services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ChatApp.Models.User;
import com.ChatApp.Models.Users;

@Service
public class UserActionHandle {
	private User user;

	@Autowired
	private FirebaseConfig fb;
	
	@Autowired
	private FirebaseConfiguration fbs;
	
	@Autowired 
	private ProfileUpdate profileUpdate;
	
	

	
	public boolean checkUserDetailsIsValid(User user)
	{
	    boolean checkDetail=true;

		if(user.getEmail().contentEquals("")|| user.getName().contentEquals("")||user.getPhone().contentEquals(""))
			checkDetail=false;
		else if(!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$"))
			checkDetail=false;
		else if(!user.getPhone().matches("\\d+"))
			checkDetail=false;
		else if(user.getName().matches("^[0-9+_.-]+@(.+)$"))
			checkDetail=false;
		
		
		if(checkDetail)
		{
			fb.addUserIntoDb(user);
			return checkDetail;
		}
		else
			return checkDetail;
			
		
	}
	public Map<String, Object> isValidUser(String email, String password) throws Exception {
	    return fb.checkCredentials(email, password);
	}
	

	public String deleteUserMessagesById(String id,String senderId) throws InterruptedException, ExecutionException
	{
		return (fb.deleteUserMessagesById(id,senderId));
	}
	public boolean forgotPassword(String email) {
		// TODO Auto-generated method stub
		return (fb.forgotPassword(email));
	}
	public boolean updatePassword(String emailTo, String password) throws Exception {
		// TODO Auto-generated method stub
		return (fb.passwordUpdate(emailTo,password));
	
	}
	
	public boolean tokenIsValid(String token) throws InterruptedException, ExecutionException
	{
		return (fb.tokenIsValid(token));
	}
	
	public Map<String,Object>editMessage(String message,String messageId) throws InterruptedException, ExecutionException
	{
		return fb.editMessage(message,messageId);
	}
	public boolean editUserDetail(Map<String, String> user) {
		return fb.editUserDetail(user);
	}
	public boolean addFriend(Map<String, String> detail) {
		
		return fb.addFriend(detail);
	}
	public Map<String, Map<String, String>> getFriends(String userEmail) {
	    Map<String, Map<String, String>> response = new HashMap<>();

	    if (!fb.checkEmail(userEmail)) {
	        Map<String, String> errorInfo = new HashMap<>();
	        errorInfo.put("Status", "False");
	        errorInfo.put("Message", "Please Login first");
	        response.put("Error", errorInfo);
	        return response;
	    }

	    return fbs.getFriends(userEmail);
	}
	public Map<String, Object> getSenderId(String email) {
		return fbs.getSenderId(email);
	}
	public String getSenderName(String senderId) {
		return fbs.getSenderName(senderId);
	}
	public void setTokenIsExpired(String token) throws InterruptedException, ExecutionException {
		fb.setTokenIsExpired(token);
		
	}
	public HashMap<Object, Object> getAllUserDetail(String userName, String password) throws InterruptedException, ExecutionException {
		return fb.getAllUserDetail(userName,password);
	}
	public Map<Integer, List< Object>> getAllMessages(String senderId,String reciverID)
	{
		return fb.getAllMessages(senderId, reciverID);
	}
	public String extractUserId(String token) {
		// TODO Auto-generated method stub
		return fb.extractUserId(token);
	}
	public String checkEmail(String currentEmail)
	{
		return profileUpdate.checkEmail(currentEmail);
	}
	
	public String updateProfile(HashMap<String,Object>profileData) throws Exception
	{
		return profileUpdate.updateProfile(profileData);
	}
	
	
	public HashMap<String, Object> unReadCount(String senderId,String reciverId) throws InterruptedException, ExecutionException {
		
		return fb.unReadCount(senderId,reciverId);
	}
	public Map<String, Object> isValidUser(String email) {
		// TODO Auto-generated method stub
		return fb.isValidUser(email);
		
	}
	public String updateUserProfilePicture(String userId, MultipartFile file) throws Exception {
		// TODO Auto-generated method stub
		return fb.updateUserProfilePicture(userId,file);
	}
	
	public String getProfileUrl(String email) throws InterruptedException, ExecutionException
	{
		return fb.getProfileUrl(email);
	}
	
	
	
	
	
}
