package com.ChatApp.Services;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ChatApp.Models.MessageContent;

@Service
public class MessagesUtils {

	
	@Autowired 
	private FirebaseConfig fb;
	
	public boolean saveMessage(MessageContent messContent)
	{
		return(fb.saveMessage(messContent));
	}
	
	public Map<Integer, List< Object>>displayMessage(String senderId,String reciverId){
		
		Map<Integer, List< Object>>result=(fb.displayMessage(senderId,reciverId));
		return result;
	}

	   
		public void isChatOpen(String senderId, String reciverId) {
			// TODO Auto-generated method stub
			fb.isChatOpen(senderId,reciverId);
			
		}
}
