package com.ChatApp.Services;

import java.io.FileInputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.cloud.Timestamp;
import java.time.ZoneId;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.ChatApp.Models.MessageContent;

import com.ChatApp.Models.User;
import com.ChatApp.Models.Users;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import com.google.cloud.firestore.WriteBatch;
import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class FirebaseConfig {
	
	 @Value("${firebase.credentialFilePath}")
	 private   String credentialFilePath;
    @Value("${firebase.dataBaseUrl}")
	 private String dataBaseUrl;
	 String userId,email,password ;
	 private Firestore db ;
	 @Autowired
     private PasswordUtil passwordUtil;
	 @Autowired 
	 private MessageEncrypt mse;
	 
	 @Autowired 
	 private JwtUtil jwtUtil;
	 @Autowired
	 private SupabaseStorageService supabaseStorage;

       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

	 
	 private HashMap<String,String>emailAndPassword=new HashMap<>();
	 private HashMap<String,String>emailAndUserId=new HashMap<>();
       
	 
	
	    @PostConstruct
	    public void FirebaseConfig() {
	        try {
	            FileInputStream serviceAccount = new FileInputStream(credentialFilePath);

	            FirebaseOptions options = FirebaseOptions.builder()
	            	    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialFilePath)))
	            	    .setDatabaseUrl(dataBaseUrl)
	            	    .build();

	            if (FirebaseApp.getApps().isEmpty()) {
	                FirebaseApp.initializeApp(options);
	              
	                
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    public boolean checkEmail(String email)
	    {
	    	 db =  (Firestore)FirestoreClient.getFirestore();
	    	 try {
	    	 ApiFuture<QuerySnapshot> future = db.collection("CredentialsData")
	                 .whereEqualTo("email", email)
	                 .limit(1)
	                 .get();	    
	    	  List<QueryDocumentSnapshot> documents= future.get().getDocuments();
				 
				 return !documents.isEmpty();
	    	 }catch(Exception e)
	    	 {
	    		 return false;
	    	 }
			        
			
			
			
		
}
		       
	    	  

	    
	    public String getUserId(String email)
	    {
	    	 db =  (Firestore)FirestoreClient.getFirestore();
	   	  ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
	   	  List<QueryDocumentSnapshot> documents;
			try {
				documents = future.get().getDocuments();
				 for(DocumentSnapshot document :documents)
			        {
			            if(document.getString("email").equals(email))
			            {
			        
			            	return document.getString("userId");
			            }
			        }
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return  null;
			
			}
			return null;

	    }
	    public void addUserIntoDb(User user)
	    {
	    	  db =  (Firestore)FirestoreClient.getFirestore();
  		    Map<String, Object> data = new HashMap<>();  
  		    data.put("name",user.getName());
  		    data.put("email", user.getEmail());
  		    data.put("phoneNumber", user.getPhone());
  		    this.userId= UUID.randomUUID().toString();
  		    data.put("userId",this.userId);
  		    db.collection("RegisterNewUser").document(this.userId).set(data);
  		    this.email=user.getEmail();
  		    this.password=user.getPassword();
  		    emailAndPassword.put(this.email, this.password);
  		    emailAndUserId.put(this.email,this.userId);
	    }
	    public void saveCredentialsIntoDb(String userEmail) throws Exception
	    {
	    	
		        
	  	  		    Map<String, Object> credentialsData = new HashMap<>();  
	  	  	  	credentialsData .put("userId",emailAndUserId.get(userEmail));
	  	  		credentialsData .put("email", userEmail);
	  	  	   credentialsData.put("password", passwordUtil.encryptPassword(emailAndPassword.get(userEmail)));
	 		    db.collection("CredentialsData").document(emailAndUserId.get(userEmail)).set(credentialsData);
			 
			
	  		    
	    }
	   
	    public void deleteRecordFromUserCollection(String userEmail)
	    {
	    	
 		    db.collection("RegisterNewUser").document(emailAndUserId.get(userEmail)).delete();

	    }
	    public void deleteOtp()
	    {
	    	db =  (Firestore)FirestoreClient.getFirestore();
		   	  ApiFuture<QuerySnapshot> future = db.collection("otpSave").get();
		   	  List<QueryDocumentSnapshot> documents;
				try {
					documents = future.get().getDocuments();
					for(DocumentSnapshot doc:documents)
					{
						Long otp=doc.getLong("Otp");
						System.out.println("OTP" +otp);
						Timestamp startTimestamp =(Timestamp) doc.get("Created");
						Timestamp endTimestamp = Timestamp.now();
						Instant startInstant = Instant.ofEpochSecond(
						        startTimestamp.getSeconds(),
						        startTimestamp.getNanos()
						);

						Instant endInstant = Instant.ofEpochSecond(
						        endTimestamp.getSeconds(),
						        endTimestamp.getNanos()
						);
						Duration duration = Duration.between(startInstant, endInstant);


			    	  if(duration.toMinutes()>5)
			    	  {
			    		  db.collection("otpSave").document((String.valueOf(otp))).delete();

			    	  }
						
						
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	    }
	  		    
	    	
	    public boolean otpSaveIntoDb(String to,int otp,LocalDateTime start)
	    {
	    	   Map<String, Object>otpSaveForRegistration = new HashMap<>();  
	    	   otpSaveForRegistration.put("Email",to);
	    	   Instant instant =
	    		        start.atZone(ZoneId.systemDefault()).toInstant();

	    		Timestamp timestamp =
	    		        Timestamp.ofTimeSecondsAndNanos(
	    		                instant.getEpochSecond(),
	    		                instant.getNano()
	    		        );
	    		
	    	   otpSaveForRegistration.put("Otp",otp);
	    	   otpSaveForRegistration.put("Created",timestamp);
	    	   
	    	   db.collection("otpSave").document(String.valueOf(otp)).set(otpSaveForRegistration);
	    	   return true;

	    }
	    public ArrayList<String> getUserEmail(int otp) throws InterruptedException, ExecutionException
	    {
	    	
	    	 DocumentReference docRef = db.collection("otpSave").document(String.valueOf(otp));

	            ApiFuture<DocumentSnapshot> future = docRef.get();
	            DocumentSnapshot document = future.get();
	            ArrayList<String>tem=new ArrayList<>();
	            if(document.exists())
	            {
	            	tem.add((String) document.get("Email"));
	            	String otpValue=String.valueOf(document.get("Otp"));
 	            	tem.add(otpValue);
	            	return tem;
	            }
	            else
	            	return tem;
	    }
	    public void deleteOtpAndEmail(int otp)
	    {
 		    db.collection("otpSave").document(String.valueOf(otp)).delete();

	    }
	    
	    public  Map<String ,Object> checkCredentials(String userEmail,String userPassword) throws Exception {
	         db = FirestoreClient.getFirestore();
              Map<String ,Object>response=new HashMap<>();
	        ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
	        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

              
	        for (DocumentSnapshot doc : documents) {
	           
	              String tempUserId=doc.getString("email");
	              String temPassword=(doc.getString("password"));  
	              String encryptedPassword=passwordUtil.encryptPassword(userPassword);
	        if(tempUserId.equals(userEmail))
	        {
	     
	        	if(temPassword.equals(encryptedPassword))
	        	{
	        	  String token=jwtUtil.generateToken(userEmail);
	      	    Map<String, Object> accessToken = new HashMap<>();  
	  	  	  	accessToken .put("jwtToken",token);
	  	  	    accessToken .put("isExpired", false);
	  	  	   accessToken.put("timeStamp",Timestamp.now());
	  		  Firestore tempDb=FirestoreClient.getFirestore();

	 		    tempDb.collection("Store_Jwt_tokens").document(token).set(accessToken);
	        	  response.put("Status", true);
	        	  response.put("token", token);
	        	  response.put("Profile_Url", doc.getString("profileImageUrl"));
	        	  response.put("Message","Login Successfully !" );
	        	  
	        		return response;
	        	}
	        	else
	        	{
	        		 response.put("Status", false);
		        	  response.put("Message","Password is wrong" );
	        		return response;
	        	}
	        }
	        else if(temPassword.equals(encryptedPassword))
	        {
	        	if(tempUserId.equals(userEmail))
	        	{
	        		 String token=jwtUtil.generateToken(userEmail);
	        		 Map<String, Object> accessToken = new HashMap<>();  
	 	  	  	  	accessToken .put("jwtToken",token);
	 	  	  	    accessToken .put("isExpired", false);
	 	  	  	   accessToken.put("timeStamp",Timestamp.now());
	 	  		  Firestore tempDb=FirestoreClient.getFirestore();

	 	 		    tempDb.collection("Store_Jwt_tokens").document(token).set(accessToken);
	 	        	  response.put("Status", true);
	 	        	  response.put("token", token);
		        	  response.put("Profile_Url", doc.getString("profileImageUrl"));

	 	        	  response.put("Message","Login Successfully !" );

                
	 	        		return response;

	        	}
	        	else
	        	{
	        		 response.put("Status", false);
		        	  response.put("Message","Email is wrong" );
	        		return response;
	        		
	        	}
	        }
	       
	        }
	        response.put("Status", false);
	        response.put("Message", "Email id & Password both are wrong");
	        return response;
	    }
	    
	    public boolean saveMessage(MessageContent messContent)
	    {

	  		    Map<String, Object> messageData = new HashMap<>();  
	  		    
	  		    try {
			    	  db =  (Firestore)FirestoreClient.getFirestore();
			  		   String messageId= UUID.randomUUID().toString();
			  		   messContent.setMessageId(messageId);
            

                       
                        messageData.put("messageId", messContent.getMessageId());
		  		messageData.put("messageContent",mse.encrypt(messContent.getMessageContent()));
	  		
		  		 messageData.put("messageType", messContent.getMessageType());
		  		 messageData.put("time", messContent.getTime());
		  		 messageData.put("date", messContent.getDate());
		  		 
	  		 
		  			 messageData.put("isRead", false);
		  			 messageData.put("isEdited", false);
		  			 messageData.put("isChatOpen", false);		  		 
		  		
	  		  messageData .put("senderId",messContent.getSenderId());
	  		  messageData .put("reciverId",messContent.getReciverId());
	  		  

		    db.collection("MessageData").document(messContent.getMessageId()).set(messageData);
		    db.collection("Back-Up-Messages").document(messContent.getMessageId()).set(messageData);
		    return true;
	  		    }catch(Exception e)
	  		    {
	  		    	System.out.println(e);
	  		    	return false;
	  		    }
	    }
//	    public void saveRecentMessages(MessageContent message)
//	    {
//	    	  String key = "messageId" + message.getMessageId();
//
//	          // Save in Sorted Set
//	    	  redisTemplate.opsForList().rightPush(key, message);
//	    	  System.out.println(redisTemplate.toString());
//	    	  System.out.println("Key "+key);
//	          // Expire after 2 days
//	          redisTemplate.expire(key, Duration.ofDays(2));
//	    }
	 public Map<Integer, List<Object>> displayMessage(String senderId, String reciverID) {
	db = (Firestore) FirestoreClient.getFirestore();
	Map<Integer, List<Object>> messageDetails = new HashMap<>();

	try {
		ApiFuture<QuerySnapshot> future1 = db.collection("MessageData")
				.whereEqualTo("senderId", senderId)
				.whereEqualTo("reciverId", reciverID)
				.get();
		ApiFuture<QuerySnapshot> future2 = db.collection("MessageData")
				.whereEqualTo("senderId", reciverID)
				.whereEqualTo("reciverId", senderId)
				.get();

		List<QueryDocumentSnapshot> future1Docs = future1.get().getDocuments();
		List<QueryDocumentSnapshot> future2Docs = future2.get().getDocuments();

		// Mark future2 messages (sent TO senderId) as read, since senderId
		// is the one viewing them right now. Batch the writes instead of
		// one update() call per document.
		WriteBatch batch = db.batch();
		boolean hasUpdates = false;
		for (QueryDocumentSnapshot doc : future2Docs) {
			Boolean isRead = doc.getBoolean("isRead");
			if ((isRead == null || !isRead) && doc.getString("reciverId").equalsIgnoreCase(senderId)) {
				batch.update(doc.getReference(), "isRead", true);
				hasUpdates = true;
			}
		}
		if (hasUpdates) {
			batch.commit().get(); // wait so the read below reflects the update
		}

		List<QueryDocumentSnapshot> documents = new ArrayList<>();
		documents.addAll(future1Docs);
		documents.addAll(future2Docs);

		Set<String> deletedMessageIds = new HashSet<>();
		ApiFuture<QuerySnapshot> deleteFuture = db.collection("MessageDelete")
				.whereEqualTo("senderId", senderId)
				.whereEqualTo("isDeleted", true)
				.get();
		for (DocumentSnapshot doc : deleteFuture.get().getDocuments()) {
			deletedMessageIds.add(doc.getString("messageId"));
		}

		int i = 0;
		for (QueryDocumentSnapshot document : documents) {
			String messageId = document.getString("messageId");
			if (deletedMessageIds.contains(messageId)) continue;

			boolean isFromFuture2 = future2Docs.contains(document);

			List<Object> messageDetail = new ArrayList<>();
			messageDetail.add("messageContent : " + mse.decrypt(document.getString("messageContent")));
			messageDetail.add("senderId : " + document.getString("senderId"));
			// Reflect the updated read state in the response too, since the
			// in-memory `document` snapshot still shows the pre-update value.
			boolean isReadValue = isFromFuture2 ? true : Boolean.TRUE.equals(document.getBoolean("isRead"));
			messageDetail.add("isRead:" + isReadValue);
			messageDetail.add("time : " + document.get("time"));
			messageDetail.add("date : " + document.get("date"));
			messageDetail.add("messageId : " + messageId);

			messageDetails.put(i++, messageDetail);
		}

	} catch (Exception e) {
		System.out.println(e);
		return null;
	}
	return messageDetails;
} 
	    public String deleteUserMessagesById(String messageId,String senderId) throws InterruptedException, ExecutionException {
	    	  db =  (Firestore)FirestoreClient.getFirestore();
	    	  DocumentReference docRef = db.collection("MessageData").document(messageId);
  			
              
        	   
	   			 ApiFuture<DocumentSnapshot> future = docRef.get();
	   			 DocumentSnapshot document = future.get();
	   			 String id=document.getString("senderId");
	   			 if(id.equals(senderId))
	   			 {
	   	    	  db =  (Firestore)FirestoreClient.getFirestore();

	   				Map<Object, Object> messageDelete = new HashMap<>();  
	 	    	   messageDelete .put("senderId",senderId);
	 	    	   messageDelete .put("messageId", messageId);
	 	    	   messageDelete.put("timeStamp", Timestamp.now());
	 	    	   messageDelete.put("isDeleted",true);
	 	 		    db.collection("MessageDelete").document(UUID.randomUUID().toString()).set(messageDelete);
	 	 		    return "Message Deleted ";
	   			 }
	   		     return null;	
	   			 
//	    	   Map<Object, Object> messageDelete = new HashMap<>();  
//	    	   messageDelete .put("senderId",senderId);
//	    	   messageDelete .put("messageId", messageId);
//	    	   messageDelete.put("timeStamp", Timestamp.now());
//	    	   messageDelete.put("isDeleted",true);
//	 		    db.collection("MessageDelete").document(UUID.randomUUID().toString()).set(messageDelete);
//	 		    return "Message Deleted ";
//	    	  
		}
		public boolean forgotPassword(String email) {
			 db = FirestoreClient.getFirestore();
	           
		        ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
		        try {
					List<QueryDocumentSnapshot> documents = future.get().getDocuments();
					 for (DocumentSnapshot doc : documents) {
				           
			              String tempUserEmail=doc.getString("email");
			              if(email.equals(tempUserEmail))
			              {
			            	  return true; 
			              }
					 }
					
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
				
					e.printStackTrace();
					return false;
				}
		        return false;

		}
		public boolean passwordUpdate(String emailTo, String password) throws Exception {
			
			String encryptedPass=passwordUtil.encryptPassword(password);
			db = FirestoreClient.getFirestore();
			String userId=getUserId(emailTo);
            if(userId!=null)
            {
            	DocumentReference docRef = db.collection("CredentialsData").document(userId);
    			
                
         	   
   			 ApiFuture<DocumentSnapshot> future = docRef.get();
   			 DocumentSnapshot document = future.get();
   			 
   		       if(document.exists())
   		       {
   		    	    Map<String, Object> updates = new HashMap<>();
   		    	    updates.put("password", encryptedPass);

   		    	   docRef.update(updates);
   		    	   return true;

            }
   		    else
		       {
		    	   return false;
		    	   
		       }
				        
	    
	      		}
            else
            {
            	return false;
            }
            	
		      
		}
		public boolean tokenIsValid(String token) throws InterruptedException, ExecutionException {
			
			boolean isToken=jwtUtil.validateToken(token);
			if(!isToken)
			{
				db = FirestoreClient.getFirestore();
	 		    db.collection("Store_Jwt_tokens").document(token).delete();
	 		    return isToken;

			}
			else
			{
				db = FirestoreClient.getFirestore();
		        DocumentReference docRef = db.collection("Store_Jwt_tokens").document(token);
		        ApiFuture<DocumentSnapshot> future = docRef.get();

		        DocumentSnapshot document = future.get();

		        if (document.exists()) {
		        	if(document.getBoolean("isExpired"))
		        	{
		        		return false;
		        	}		        	
		        }
		        return isToken;
			}
		
		}
		public Map<String,Object>editMessage(String message,String messageId) throws InterruptedException, ExecutionException
		{ 
	    	  db =  (Firestore)FirestoreClient.getFirestore();

			 DocumentReference docRef = db.collection("MessageData").document(messageId);
			 ApiFuture<DocumentSnapshot> future = docRef.get();
			 DocumentSnapshot document = future.get();
			 
              Map<String,Object>response=new HashMap<>();
		       if(document.exists())
		       {
		    	    Map<String, Object> updates = new HashMap<>();
		    	    updates.put("messageContent", mse.encrypt(message));
		    	    updates.put("isEdited", true);		  	


		    	   docRef.update(updates);
		    	   response.put("Status", true);
		            response.put("Message", "Edited");
					return response;

		       }
		       else
		       {
		    	   response.put("Status", false);
		            response.put("Message", "Something went wrong");
					return response;

		       }

		    }
		public boolean editUserDetail(Map<String, String> user) {
			 
			 db =  (Firestore)FirestoreClient.getFirestore();
	    	  ApiFuture<QuerySnapshot> future = db.collection("RegisterNewUser").get();
	    	  List<QueryDocumentSnapshot> documents;
			try {
				documents = future.get().getDocuments();
				 for(DocumentSnapshot document :documents)
			        {
			            if(document.getString("email").equals(user.get("email")))
			            {
			            	 DocumentReference docRef = db.collection("RegisterNewUser").document(document.getString("userId"));
			    			 ApiFuture<DocumentSnapshot> futures = docRef.get();
			    			 DocumentSnapshot documented = futures.get();
			            	 Map<String, Object> updates = new HashMap<>();
			            	 if(user.get("name").trim().equalsIgnoreCase("") || user.get("phoneNumber").trim().equalsIgnoreCase(""))
			            	 {
			            		 return false;
			            	 }
			            	 
					    	    updates.put("name", user.get("name"));
					    	    updates.put("phoneNumber", user.get("phoneNumber"));

					    	   docRef.update(updates);
					    	 
								return true;	
			            }
			        }
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}
			            	
		
			
			return false;
		}

		public boolean alreadyFriends(String friendUId,String userUId) throws InterruptedException, ExecutionException
		{
			 db = FirestoreClient.getFirestore();
	           
		        ApiFuture<QuerySnapshot> future = db.collection("FriendList").get();
		    	List<QueryDocumentSnapshot> documents = future.get().getDocuments();
				 for (DocumentSnapshot doc : documents) {
					 if(doc.getString("friendUserId").equalsIgnoreCase(friendUId) && doc.getString("fromUserId").equalsIgnoreCase(userUId))
					 {
						return true; 
					 }
					 
				 }
				 return false;
		        
		}
			      
		public boolean addFriend(Map<String,String>detail)
		{
			if(checkEmail(detail.get("friendEmail")))
					{
				String friendUId = null,userUId = null,friendProfileUrl=null,userProfile = null;
					
				 db = FirestoreClient.getFirestore();
		           
			        ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
			        try {
						List<QueryDocumentSnapshot> documents = future.get().getDocuments();
						 for (DocumentSnapshot doc : documents) {
					           
				              if(detail.get("friendEmail").equalsIgnoreCase(doc.getString("email")))
				              {
				            	  friendUId=doc.getString("userId");
				            	  friendProfileUrl=doc.getString("profileImageUrl");
				              }
				              if(detail.get("email").equalsIgnoreCase(doc.getString("email")))
				              {
				            	  userUId=doc.getString("userId");
				            	  userProfile=doc.getString("profileImageUrl");

				            	  
				            	  
				            	  
				              }
					}
						 if(!alreadyFriends(friendUId,userUId))
						 {
						  Map<String, Object> data = new HashMap<>();  
				  		    data.put("fromUserId",userUId);
				  		    data.put("friendUserId", friendUId);
				  		    data.put("from",detail.get("email") );
				  		   String id=UUID.randomUUID().toString(); 
				  				   data.put("requestId",  id);
				  		    data.put("Add-Time", LocalTime.now().format(formatter));
				  		    data.put("Date",LocalDate.now().toString() );
				  		    data.put("FriendProfileImageUrl", friendProfileUrl);
				  		    data.put("userProfile",userProfile);

				  		    
				  		 
				  		    db.collection("FriendList").document(id).set(data);
				  		    return true;
						 }
						 return false;
						 
			        }catch(Exception e)
			        {
			        	System.out.println("Exception occured during add friend");
			        	System.out.println(e);
			        	return false;
			        }
					}
			else
			{
				return false;
			}
	
}
		public void setTokenIsExpired(String token) throws InterruptedException, ExecutionException {
			
			db = FirestoreClient.getFirestore();

	        DocumentReference docRef = db.collection("Store_Jwt_tokens").document(token);

	        ApiFuture<WriteResult> future = docRef.update("isExpired", true);
			
		}
		public HashMap<Object, Object> getAllUserDetail(String userName, String password) throws InterruptedException, ExecutionException {
			
			HashMap<Object,Object>result = new HashMap<>();
			String adminUserId="vk368065@gmail.com";
			String adminPassword="8218394110";
			if(adminUserId.equalsIgnoreCase(userName)&& adminPassword.equalsIgnoreCase(password))
			{
		        result.put("Status", true);
		        result.put("token", jwtUtil.generateToken(adminUserId));
       		


			db = FirestoreClient.getFirestore();
			  ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
              List<Object>tempData=new ArrayList<>();

		        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		        int i=0;
		        for(DocumentSnapshot doc:documents)
		        {
		        	 String email=doc.getString("email");
		        	 Firestore temDb=FirestoreClient.getFirestore() ;
		        	 ApiFuture<QuerySnapshot>futures=temDb.collection("RegisterNewUser").get();
				     List<QueryDocumentSnapshot> temDocuments = futures.get().getDocuments();
				     for(DocumentSnapshot temDoc:temDocuments)
				     {
				    	 if(email.equalsIgnoreCase(temDoc.getString("email")))
				    	 {
				    		 tempData.add("Name : "+temDoc.getString("name"));
				    		 tempData.add("Email : "+email);
				    		 tempData.add("userId : "+doc.getString("userId"));
				    		 break;
				    	 }
				     }
				     result.put(i++, tempData);
				     tempData=new ArrayList<>();
		        	 
		        	 

		        	 
		        }
		        return result;
		        
			}
			else
			{
				result.put("Status", false);
				result.put("Message", "UserName or Password is wrong");
				return result;
			}
			

			
		}
		
		  public Map<Integer, List< Object>> getAllMessages(String senderId,String reciverID)
		    {
		    	  db =  (Firestore)FirestoreClient.getFirestore();
	              List<Object>messageDetail=new ArrayList<>();

		    	Map<Integer, List<Object>> messageDetails =new HashMap<Integer,List< Object>>();
		    	  ApiFuture<QuerySnapshot> future = db.collection("Back-Up-Messages").get();
		    	  try {
		    		  int i=0;
			        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
			        for(DocumentSnapshot document :documents)
			        {
			        	
			        	String senderIds=document.getString("senderId");
			        	String reciverId=document.getString("reciverId");
			        	if(reciverID.equals(reciverId) && senderIds.equals(senderId))
			        	{
			    			
			        		messageDetail.add("messageContent : "+ mse.decrypt(document.getString("messageContent")));
			        	
			        		messageDetail.add("senderId : "+document.getString("senderId"));
				        		messageDetail.add("Status : " +"Read");

			        		

			        		messageDetail.add("time : "+ document.get("time"));
			        		messageDetail.add("date : "+ document.get("date"));
			        		messageDetail.add("messageId : "+ document.getString("messageId"));

			        		
			        		
			        		messageDetails.put(i++, messageDetail);
			        		messageDetail=new ArrayList<>();
			        		}
			        		
			        	}
		    	  }catch(Exception e)
		    	  {
		    		  System.out.println(e);
		    		  return null;
		    	  }

				return messageDetails;

		    	
		    	
		    }
		public String extractUserId(String token) {
			return jwtUtil.extractUsername(token);
		}
		public boolean updateProfile(HashMap<String,Object>profileData) throws InterruptedException, ExecutionException
		{
			
			db=(Firestore)FirestoreClient.getFirestore();
			ApiFuture<QuerySnapshot> future = db.collection("CredentialsData")
	                .whereEqualTo("email", profileData.get("currentEmail"))
	                .limit(1)
	                .get();

	        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

	        if (documents.isEmpty()) {
	            return false;
	        }

	        
	        documents.get(0)
	                .getReference()
	                .update("email", profileData.get("newEmail"));
	        future=db.collection("FriendList").whereEqualTo("from", profileData.get("currentEmail")).limit(1).get();
	        documents=future.get().getDocuments();
	        documents.get(0)
            .getReference()
            .update("from", profileData.get("newEmail"));
	        

	        return true;
			
			
		}

	

   	   

		
	
		
		public HashMap<String, Object> unReadCount(String senderId,String reciverID) throws InterruptedException, ExecutionException {
			  db =  (Firestore)FirestoreClient.getFirestore();
	    	HashMap<String,Object> response =new HashMap<>();
	    	int unReadCount=0;
	    	  ApiFuture<QuerySnapshot> future = db.collection("MessageData").get();
	    	  
	    		 
		        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		        for(DocumentSnapshot document :documents)
		        {
		        	
		        	String senderIds=document.getString("senderId");
		        	String reciverId=document.getString("reciverId");
		        	if(reciverID.equals(reciverId) && senderIds.equals(senderId))
		        	{
		        		
		        		if(!document.getBoolean("isChatOpen"))
			        	{
		        			if(!document.getBoolean("isRead") )
			        		{
			        			unReadCount++;
			        		}	
			        	}
		        	}
		        	
		        }
		        response.put("UnReadCount", unReadCount);
		        response.put("ReciverId", reciverID);
		        return response;
		        
		        
}
		public Map<String, Object> isValidUser(String email) {
			// TODO Auto-generated method stub
			HashMap<String,Object>response=new HashMap<>();
			if(checkEmail(email)) {
			 String token=jwtUtil.generateToken(email);
    		 Map<String, Object> accessToken = new HashMap<>();  
	  	  	  	accessToken .put("jwtToken",token);
	  	  	    accessToken .put("isExpired", false);
	  	  	   accessToken.put("timeStamp",Timestamp.now());
	  		  Firestore tempDb=FirestoreClient.getFirestore();

	 		    tempDb.collection("Store_Jwt_tokens").document(token).set(accessToken);
	        	  response.put("Status", true);
	        	  response.put("token", token);
	        	  response.put("Message","Login Successfully !" );
        	 
        
	        		return response;
			}
			else
			{
			
				response.put("Status",false);
	        	  response.put("Message","Access Denied" );
	        	  return response;
			}

		}
		public void isChatOpen(String senderId, String reciverId) {
    db = (Firestore) FirestoreClient.getFirestore();
    try {
        List<QueryDocumentSnapshot> docs = db.collection("MessageData")
                .whereEqualTo("senderId", reciverId)
                .whereEqualTo("reciverId", senderId)
                .get().get().getDocuments();

        WriteBatch batch = db.batch();
        boolean hasUpdates = false;
        for (QueryDocumentSnapshot doc : docs) {
            Boolean chatOpen = doc.getBoolean("isChatOpen");
            if (chatOpen == null || !chatOpen) {
                batch.update(doc.getReference(), "isChatOpen", true);
                hasUpdates = true;
            }
        }
        if (hasUpdates) batch.commit(); // no need to block here unless a caller depends on it
    } catch (Exception e) {
        // log properly instead of System.out.println("Chat Open")
    }
}
		public String updateUserProfilePicture(String userId, MultipartFile file) throws Exception {
			// TODO Auto-generated method stub
			if (file.getContentType() != null &&
				    file.getContentType().startsWith("image/")) {
				if(checkEmail(userId))
				{
					String imageUrl=	supabaseStorage.uploadImage(file);
					db = FirestoreClient.getFirestore();
					String tempUserId=getUserId(userId);
		            if(tempUserId!=null)
		            {
		            	DocumentReference docRef = db.collection("CredentialsData").document(tempUserId);
		         	   
		   			 ApiFuture<DocumentSnapshot> future = docRef.get();
		   			 DocumentSnapshot document = future.get();
		   		
		   		    	    Map<String, Object> updates = new HashMap<>();
		   		    	 updates.put("profileImageUrl", imageUrl);
		   		    	   docRef.update(updates);
		   		    	   return "Profile picture updated successfully";
		   	}
		            else
		            {
						return "Image not saved ";

		            }
					
			}
				else
				{
					return "User not exist";
				}

				} else {

				    return("Please Upload an image");
				}
			
		
			
		}
		public String getProfileUrl(String email) throws InterruptedException, ExecutionException
		{
			  db = FirestoreClient.getFirestore();
              Map<String ,Object>response=new HashMap<>();
	        ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
	        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

              
	        for (DocumentSnapshot doc : documents) {
	           
	              String tempUserEmail=doc.getString("email");
	              if(email.equalsIgnoreCase(tempUserEmail))
	              {
	            	  return doc.getString("profileImageUrl");
	              }
	             

		}
	        return "User Account does not exist";
	
}

	
}
		
//		public void addToken(String email,String token,String prevToken) throws InterruptedException, ExecutionException
//		{
//			
//			if(checkEmail(email))
//			{
//				
//				db=(Firestore)FirestoreClient.getFirestore();
//				ApiFuture<QuerySnapshot> future = db.collection("CredentialsData")
//		                .whereEqualTo("isToken",prevToken)
//		                .limit(1)
//		                .get();
//
//		        List<QueryDocumentSnapshot> document = future.get().getDocuments();
//		        if(!document.isEmpty())
//		        {
//		        	 document.get(0)
//		                .getReference()
//		                .update("isToken",token);
//		        	
//		        }
//		        
//
//			}
//		}
		
		
	

       
			
