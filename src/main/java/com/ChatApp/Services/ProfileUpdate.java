package com.ChatApp.Services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ProfileUpdate {
	

	

	@Autowired
	private FirebaseConfig fb;
	@Autowired
	private EmailService emailSer;
	
	
	
 
	public String checkEmail(String email)
	{
		if(fb.checkEmail(email))
		{
			 return "This email address is used by someone else please enter other email";
			 
		}
		else
		{
			emailSer.sendEmail(email);
			return "Otp send successfully !";
		}
	}
	public String updateProfile(HashMap<String,Object>profileData) throws Exception
	{
		boolean result=emailSer.verifyOtpUpdate((Integer.parseInt( (String) profileData.get("otp"))));
		if(result){
			if(fb.updateProfile(profileData)) {
				return "Email Updated Successfully !";
			}
			else
			{
				return "Something went wrong";
			}
			
			
		}
		else
		{
			return "Otp is wrong or invalid";
		}
	

	}
	}

