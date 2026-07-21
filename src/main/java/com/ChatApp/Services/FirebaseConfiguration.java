package com.ChatApp.Services;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldMask;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class FirebaseConfiguration {

	@Value("${firebase.credentialFilePath}")
	 private   String credentialFilePath;
   @Value("${firebase.dataBaseUrl}")
	 private String dataBaseUrl;
	private Firestore db ;

	
	 public FirebaseConfiguration() {
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
	 
public Map<String, Map<String, String>> getFriends(String userEmail) {
    db = (Firestore) FirestoreClient.getFirestore();
    Map<String, String> profileImageMap = new HashMap<>(); // userId -> profile image URL (from FriendList)
    Map<String, Map<String, String>> response = new HashMap<>();
    List<QueryDocumentSnapshot> documents;
    try {
        ApiFuture<QuerySnapshot> future = db.collection("FriendList").get();
        documents = future.get().getDocuments();

        for (DocumentSnapshot doc : documents) {
            String from = doc.getString("from");
            if (from != null && from.equalsIgnoreCase(userEmail)) {
                String friendId = doc.getString("friendUserId");
                String userId = doc.getString("fromUserId");

                if (friendId != null) {
                    // friend's photo, straight from FriendList
                    profileImageMap.put(friendId, doc.getString("FriendProfileImageUrl"));
                }
                if (userId != null) {
                    // your own photo, straight from FriendList
                    profileImageMap.put(userId, doc.getString("userProfile"));
                }
            }
        }

        for (String temp : profileImageMap.keySet()) {
            DocumentReference docRef = db.collection("RegisterNewUser").document(temp);
            FieldMask mask = FieldMask.of("name"); // only need name here now
            ApiFuture<DocumentSnapshot> futureSnap = docRef.get(mask);
            DocumentSnapshot snap = futureSnap.get();

            if (!snap.exists()) {
                continue;
            }

            Map<String, String> friendInfo = new HashMap<>();
            friendInfo.put("name", snap.getString("name"));
            friendInfo.put("userProfile", profileImageMap.get(temp));

            response.put(temp, friendInfo);
        }

        return response;
    } catch (Exception e) {
        System.out.println("Exception occurred during getFriends List");
        System.out.println(e);
        return response;
    }
}	public Map<String, Object> getSenderId(String email) {
		
		 db =  (Firestore)FirestoreClient.getFirestore();
		 Map<String,Object>response=new HashMap<>();
   	  ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
   	  List<QueryDocumentSnapshot> documents;
		try {
			documents = future.get().getDocuments();
			 for(DocumentSnapshot document :documents)
		        {
		            if(document.getString("email").equals(email))
		            {
		            	response.put("Status", true);
		            	response.put("UserId",document.getString("userId"));
		            	return response;
		            }
		        }
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return response=null;
		
		}
		return response=null;
	}

	public String getSenderName(String senderId) {
		// TODO Auto-generated method stub
		 db=(Firestore)FirestoreClient.getFirestore();
		  DocumentReference docRef=  db.collection("RegisterNewUser").document(senderId);
		   FieldMask mask = FieldMask.of(("name"));
		  ApiFuture<DocumentSnapshot> futures = docRef.get(mask);
		  try {
			return (futures.get().getString("name"));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return "";
		
	}
//	public boolean checkUserIdentity(String email,String token) throws InterruptedException, ExecutionException
//	{
//		 db = FirestoreClient.getFirestore();
//        ApiFuture<QuerySnapshot> future = db.collection("CredentialsData").get();
//        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
//
//         
//        for (DocumentSnapshot doc : documents) {
//        	if(doc.getString("email").equals(email) && doc.getString("isToken").equals(token))
//        	{
//        		return true;
//        }
//        }
//        return false;
//	}
	

			
	
	 
}
