 package com.ChatApp.Common_Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ChatApp.Services.SupabaseStorageService;
import com.ChatApp.Services.UserActionHandle;
import com.google.api.client.util.Value;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@CrossOrigin

public class HandleRequest2 {
	
	@Autowired
	private UserActionHandle userHandle;
	
	
	
	@PostMapping("/get_friends")
	public ResponseEntity<Map<String,Map<String, String>>> getFriends(@RequestParam String userEmail,@RequestHeader("Authorization")String token) throws InterruptedException, ExecutionException
	{
		Map<String, Map<String, String>>response=new HashMap<>();
		if(userHandle.tokenIsValid(token))
		{
			response=userHandle.getFriends(userEmail);
			System.out.println(response);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		else
		{
//			System.out.println(response);
//
//			response.put("Status", "False");
//			response.put("Message", "Unauthorized access");
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

	}
	
	@PostMapping("/get_senderId")
	public ResponseEntity<Map<String,Object>>getSenderId(@RequestBody String email,@RequestHeader("Authorization")String token) throws InterruptedException, ExecutionException
	{
		Map<String,Object>response=new HashMap<>();
		 JsonObject jsonObject = JsonParser.parseString(email).getAsJsonObject();
	        String userEmail = jsonObject.get("email").getAsString();
	     
		if(userHandle.tokenIsValid(token))
		{
		
			response=userHandle.getSenderId(userEmail);
			return new ResponseEntity<> (response,HttpStatus.OK);

		
		}
		else
		{
			response.put("Status", "False");
			response.put("Message", "Unauthorized access");
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}
		
	}
	
	@PostMapping("/get_sender_name")
	public String getSenderName(@RequestBody String senderId,@RequestHeader("Authorization")String token) throws InterruptedException, ExecutionException
	{
		 JsonObject jsonObject = JsonParser.parseString(senderId).getAsJsonObject();
	        String senderID = jsonObject.get("senderId").getAsString();
	        

		if(userHandle.tokenIsValid(token))
		{
			return userHandle.getSenderName(senderID);

		}
		else
		{
		   return "UNAUTHORIZED ACCESS";
		}
	}
	
	@PostMapping("/sendOtp")
	public  ResponseEntity<Map<String,Object>>  otpVerifyEmailUpdate(@RequestParam String newEmail)
	{
		System.out.println(newEmail);
		Map<String,Object>response=new HashMap<>();

		if(userHandle.checkEmail(newEmail).equalsIgnoreCase("Otp send successfully !"))
		{
			response.put("Status", true);
			response.put("Message","Otp send successfully !");
			return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

		}
		else
		{
			response.put("Status",false);
			response.put("Message","This email address is used by someone else please enter other email");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);

		}
	
	}
	@PatchMapping("/updateEmail")
	public  ResponseEntity<Map<String,Object>> updateEmail(@RequestBody HashMap<String,Object>userData) throws Exception
	{
		Map<String,Object>response=new HashMap<>();

		if(userHandle.updateProfile(userData).equalsIgnoreCase("Email Updated Successfully !")){
			response.put("Status", true);
			response.put("Message","Email Updated Successfully !");
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		else if(userHandle.updateProfile(userData).equalsIgnoreCase("Otp is wrong or invalid"))
		{
			response.put("Status",false);
			response.put("Message","Otp is wrong or invalid");
			return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

		}
		else
		{
			response.put("Status",false);
			response.put("Message","Something went wrong");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
	}
	
	   @Autowired
	    private SupabaseStorageService storageService;

	    @PatchMapping("/profile-picture-upload")
	    public ResponseEntity<Map<String,Object>> uploadImage(
	            @RequestParam("file") MultipartFile file,@RequestHeader("Authorization")String token)
	            throws Exception {
	    
			Map<String,Object>response=new HashMap<>();

	    	if(userHandle.tokenIsValid(token))
			{
	    		String email=userHandle.extractUserId(token);
				 if(userHandle.updateUserProfilePicture(email,file).equalsIgnoreCase("Profile picture updated successfully"))
 {
		response.put("Status", true);
		response.put("Message","Profile picture updated successfully");
		response.put("profileUrl", userHandle.getProfileUrl(email) );
	    
		return new ResponseEntity<>(response, HttpStatus.OK);
	 
 }
				 
						response.put("Status", false);
				response.put("Message",userHandle.updateUserProfilePicture(userHandle.extractUserId(token),file));
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
					

			}

	    	else
			{
				response.put("Status",false);
				response.put("Message","Unauthorized Access");
				return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
			}	    }

}
