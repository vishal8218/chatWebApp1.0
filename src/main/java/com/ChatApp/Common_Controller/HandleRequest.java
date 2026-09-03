 package com.ChatApp.Common_Controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.validation.Valid;

import com.ChatApp.Models.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ChatApp.Services.EmailService;
import com.ChatApp.Services.JwtUtil;
import com.ChatApp.Services.MessagesUtils;
import com.ChatApp.Services.UserActionHandle;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpServletResponse;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
@CrossOrigin("https://chat-app-lime-iota-22.vercel.app/")
public class HandleRequest  {
	
	@Autowired
	private UserActionHandle userHandle;
	@Autowired
	private EmailService emailSer;
	
	@Autowired
	private MessagesUtils messagesUtil;
	
	
	
	private String emailTo;
	private SimpMessagingTemplate messagingTemplate = null;
	
	private Principal principal;
	

	
	   public HandleRequest(SimpMessagingTemplate messagingTemplate) {
	        this.messagingTemplate = messagingTemplate;
	    }

	    @GetMapping("/")
	   public void defaultPage(HttpServletResponse response) throws IOException {
	       response.sendRedirect("https://chat-apps-murex.vercel.app/");
	   }

	    
	   @PostMapping("/admin")
	   public ResponseEntity<Map<Object,Object>> admin(@Valid @RequestBody LoginUser user) throws InterruptedException, ExecutionException
	   {
		   String userName=user.getUserEmailId();
		   String password=user.getPassword();
		   if(userName.trim().equalsIgnoreCase("") || password.trim().equalsIgnoreCase(""))
		   {
			   return null;
		   }
		   else
		   {
			   HashMap<Object,Object>result=userHandle.getAllUserDetail(userName,password);
			    if(!(boolean) result.get("Status"))
			    {
					return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);

			    }
				return new ResponseEntity<>(result, HttpStatus.OK);

		   }
		   
	   }
	  
 
	@PostMapping("/register_new_user")
	public  HashMap<String,Object> addNewUser(@RequestBody User user)
	{
         HashMap<String,Object>response=new HashMap<>();
         
		if(userHandle.checkUserDetailsIsValid(user))
		{
			
		if(emailSer.sendEmail(user.getEmail()).equalsIgnoreCase(""))
		{
		  response.put("Status", true);
		  response.put("Message", "Otp send to your email");
			return response;
		}
		else
		{
			 response.put("Status", false);
			  response.put("Message", "Email id already exit's");
				return response;	
			 
		}

		}
		 response.put("Status", false);
		  response.put("Message", "Please Enter correct details");
		return response;
	}
	
//	@PostMapping("/api/auth/google")
//	public String googleLogin(OAuth2AuthenticationToken token)
//	{
//		System.out.println(token.getAuthorizedClientRegistrationId());
//	    OAuth2User user = token.getPrincipal();
//
//		 String email = user.getAttribute("email");
//		    String name = user.getAttribute("name");
//		    System.out.println("Name "+ name);
//		    System.out.println("Email "+email);
//		    return"eyey.fgeerkknfkndnsnknd";
//	}
// 
	
	
	
	@PostMapping("/verifyotp")
	public String verifyOtp(@RequestParam int otp) throws Exception
	{
		 if(emailSer.verifyOtp(otp))
			 {
			 return "User Register Successfully !";
			 }
			 else
			 {
				 return "Otp expired or wrong otp";
			 }
	}
	
	
	

	
	
	@PostMapping("/loginwithemail")
	public  ResponseEntity<Map<String,Object>> loginUser(@RequestBody LoginUser loginUser) throws Exception 
	{
        Map<String, Object>response=userHandle.isValidUser(loginUser.getUserEmailId().toString(), loginUser.getPassword().toString());

        if(!(boolean) response.get("Status"))
        {
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        }
		return new ResponseEntity<>(response, HttpStatus.OK);
	
	}
	
	@PostMapping("/send_otp")
	public void sendOtp(@ModelAttribute String to)
	{
		this.emailTo=to;
		emailSer.sendEmail(this.emailTo);
	}
  
	
	@MessageMapping("/send_message")
	public void saveMessage(MessageContent message, Principal principal)
	{

		
		
			
	    
			if(!message.getSenderId().equals("") && !message.getReciverId().equals(""))
			{
			
		if(messagesUtil.saveMessage(message))
		{
			messagingTemplate.convertAndSendToUser(
				    message.getReciverId(),      
				    "/queue/messages",           
				    message                      
				);
			

			
			
		}
			}
	}
	@MessageMapping("/is_chat_open")
	public UserIds isChatOpen(UserIds receipt, Principal principal) {
	    // Presence signal only — do NOT mark messages as read here.
	    messagingTemplate.convertAndSendToUser(
	        String.valueOf(receipt.getSenderId()),
	        "/queue/is_chat_open",
	        receipt
	    );
	    return receipt;
	}

	@MessageMapping("/message_read")
	public UserIds markAsRead(UserIds receipt, Principal principal) {
	    messagesUtil.displayMessage(receipt.getSenderId(), receipt.getReciverId());

	    // Notify the ORIGINAL SENDER (receipt.getSenderId()), not the caller —
	    // @SendToUser was routing this back to the reader instead.
	    messagingTemplate.convertAndSendToUser(
	        String.valueOf(receipt.getSenderId()),
	        "/queue/read-receipt",
	        receipt
	    );
	    return receipt;
	}
	@MessageMapping("/edit_messages")
	public void edit(MessageContent message, Principal principal) throws InterruptedException, ExecutionException {

		
	    messagingTemplate.convertAndSendToUser(
	        message.getReciverId(),
	        "/queue/edit",
	        message
	    );
	    
	   
	}

	
	
	@PostMapping("/unread_count")
	public ResponseEntity<Map<String,Object>>  unReadCount(@RequestBody UserIds userIds,@RequestHeader ("Authorization")String token) throws InterruptedException, ExecutionException
	{
		HashMap<String,Object>response=new HashMap<>();
				if(userHandle.tokenIsValid(token))
		{
	    	response=userHandle.unReadCount(userIds.getSenderId(),userIds.getReciverId());
	    	
		return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	    else {
	    	response.put("Status", false);
	    	response.put("Message", "Unauthorized Access");
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);


	    }
	}
	






	@PostMapping("/get_messages")
	public Map<Integer, List< Object>> getMessage(@RequestBody MessageUser messageUser ,@RequestHeader ("Authorization")String token) throws InterruptedException, ExecutionException
	{
		
		 Map<Integer, List< Object>> result=new HashMap<>();
		 
		if(userHandle.tokenIsValid(token))
		{
		 result=messagesUtil.displayMessage(messageUser.getSenderId(),messageUser.getReciverId());
         if(result!=null)
         {
             this.principal = () -> messageUser.getReciverId(); 
        	 return result;
         }
		}
		List<Object> list = new ArrayList<>();
		list.add(HttpStatus.UNAUTHORIZED);
		result.put(0, list);
		return result;
		 
	
	
	}
	
	@PostMapping("/get_all_messages")
	public Map<Integer, List< Object>> getAllMessages(@RequestBody MessageUser messageUser ,@RequestHeader ("Authorization")String token)
	{
		return userHandle.getAllMessages(messageUser.getSenderId(),messageUser.getReciverId());
	}
	
	
	@DeleteMapping("/deleteById")
	public ResponseEntity<Map<String,Object>> deleteUserMessagesById(@RequestBody Map<String,String>deleteDetail,@RequestHeader ("Authorization")String token) throws InterruptedException, ExecutionException
	{
		
		Map<String,Object>response=new HashMap<>();
		if(userHandle.tokenIsValid(token))
		{
	    if(userHandle.deleteUserMessagesById(deleteDetail.get("id"),deleteDetail.get("senderId"))!=null)
	    {
	    	response.put("Status", true);
	    	response.put("Message", "Message Deleted Successfully ");
		return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	    else {
	    	response.put("Status", false);
	    	response.put("Message", "Message Not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);


	    }
		}
		else
		{
			response.put("Status", false);
	    	response.put("Message", "Unauthorized Access");
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

	}
	}
	
	@PostMapping("/forgot_password")
	public String forgotPassword(@RequestBody String userEmail)
	{
		 JsonObject jsonObject = JsonParser.parseString(userEmail).getAsJsonObject();
	        String email = jsonObject.get("userEmail").getAsString();
	        if(userHandle.forgotPassword(email))
	      {
	    	  emailSer.forgotPassword=true;
	    	  emailSer.sendEmail(email);
	    	  this.emailTo=email;
	    	  return "OTP is send to your email";
	      }
	      else
	      {
	    	  return "Account does not exit's";
	      }
	}
	
	@PostMapping("/otp_verify")
	public String passwordChange(@RequestBody Map<String ,Integer> otp)
	{ 
		   try {
			if(emailSer.verifyOtpPasswordChange(otp.get("otp")))
			   {
			  
				   return "OTP VERIFED SUCCESSFULLY !!!";
				   
			   }
		} catch (Exception e) {
			e.printStackTrace();
		}
		   return "Otp incorrect";
	}
	
	@PatchMapping("/password_update")
	public String passwordUpdate(@RequestBody String password) throws Exception
	{

		 JsonObject jsonObject = JsonParser.parseString(password).getAsJsonObject();
	        String pass = jsonObject.get("password").getAsString();
		if(userHandle.updatePassword(this.emailTo,pass))
		{
			return "Password Updated Successfully !!!";
		}
		else
		{
			return "Password not updated";
		}
	}
	
	@PatchMapping("/edit_message")
	public ResponseEntity<Map<String, Object>> editMessage(@RequestBody Map<String,String>messageDetails,@RequestHeader("Authorization") String token) throws InterruptedException, ExecutionException
	{
		  Map<String,Object>response=new HashMap<>();
		  if(userHandle.tokenIsValid(token))
		  {
			   response=userHandle.editMessage(messageDetails.get("message"),messageDetails.get("messageId")) ;
			   return new ResponseEntity<>( response,HttpStatus.OK);
		  }
		  else
		  {
			  response.put("Status", false);
				response.put("Message", "You are not authorized to access the resource");
			    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		  }

	}
	
	@PatchMapping("/edit_user_details")
	public ResponseEntity<Map<String, Object>>editUserDetails(@RequestBody Map<String,String>user,@RequestHeader("Authorization")String token) throws IllegalArgumentException, InterruptedException, ExecutionException
	{
		Map<String,Object>response=new HashMap<>();
		if(JwtUtil.validateToken(token))
		{
		 if(userHandle.editUserDetail(user))
			 {
			   response.put("Status", true);
			   response.put("Message", "User details update successfully!");
			    return new ResponseEntity<>(response, HttpStatus.OK);
			 }
		 else
		 {
			 response.put("Status", false);
			   response.put("Message", "User details are not update please try again");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		 }
		}
		else
		{
			  response.put("Status", false);
				response.put("Message", "You are not authorized to access the resource");
			    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}
		
	}
	
	@PostMapping("/upload_a_file")
	public String uploadAFile(@RequestParam("file") MultipartFile path) throws IOException
	{
		 String uploadDir = "C:\\Users\\vk368\\Downloads";
		    File saveFile = new File(uploadDir + path.getOriginalFilename());
		    path.transferTo(saveFile); 


		    PDDocument document = PDDocument.load(saveFile);
		    PDFTextStripper pdfStripper = new PDFTextStripper();
		    String text = pdfStripper.getText(document);

		    document.close();

		    return "Saved File";	
		}
	
	
@PostMapping("/logout")
public void logout(@RequestHeader("Authorization")String token) throws InterruptedException, ExecutionException
{
	     userHandle.setTokenIsExpired(token);
	      
	    	  
	     
}
	
	   @PostMapping("/add_friend")
	   public ResponseEntity<Map<String,Object>> addFriend(@RequestBody Map<String,String>detail, @RequestHeader("Authorization")String token) throws InterruptedException, ExecutionException
	   {
			Map<String,Object>response=new HashMap<>();

		   if(userHandle.tokenIsValid(token))
		   {
		   if(userHandle.addFriend(detail))
		   {
			   response.put("Status", true);
				response.put("Message", "Add user successfully !");
			    return new ResponseEntity<>(response, HttpStatus.OK);
			   
		   }
		   else
		   {
			   response.put("Status",false);
				response.put("Message", "User Not Exit's");
			    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		   }
		   }
		   else
		   {
			   response.put("Status", false);
				response.put("Message", "You are not authorized to access the resource");
			    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		   }
	   }
	   
	  
	   



   @PostMapping("/loginuser")
	public  ResponseEntity<Map<String,Object>> loginAsUser(@RequestParam String email ,@RequestHeader("Authorization")String token) throws Exception 
	{
	  
       Map<String, Object>response =new HashMap<>();
       if(userHandle.tokenIsValid(token))
       {
    	  response= userHandle.isValidUser(email);
    	   if(!(boolean) response.get("Status"))
           {
    			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

           }
   		return new ResponseEntity<>(response, HttpStatus.OK);

       }
       response.put("Message", "Access Denied");
      
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

       
	
	}
   private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
   @PostMapping("/upload")
   public ResponseEntity<String> uploadImage(
           @RequestParam("name") String name,
           @RequestParam("image") MultipartFile file,Principal principal,MessageContent message) {

       try {
           System.out.println("Name: " + name);
           System.out.println("File Name: " + file.getOriginalFilename());

           // Create directory if not exists
           Path uploadPath = Paths.get(UPLOAD_DIR);
           if (!Files.exists(uploadPath)) {
               Files.createDirectories(uploadPath);
           }

           // Clean file name (important for security)
           String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();

           // Full path
           Path filePath = uploadPath.resolve(fileName);

           // Save file
           Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
           messagingTemplate.convertAndSendToUser(
				    message.getReciverId(),      // ✅ user (the recipient ID or username)
				    "/queue/messages",message      // ✅ destination (without /user prefix)
				                      // ✅ payload (your message object)
				);

           return ResponseEntity.ok("File uploaded successfully: " + fileName);

       } catch (IOException e) {
           e.printStackTrace();
           return ResponseEntity.status(500).body("File upload failed");
       }
   }
   
   @PostMapping("/extract-text")
   public String extractText(@RequestParam("image") MultipartFile image)
           throws IOException, TesseractException {

	   BufferedImage img = ImageIO.read(image.getInputStream());

	   Tesseract t = new Tesseract();
	// String ex=  t.doOCR(img);
//	   t.setDatapath("C:\\Program Files\\Tesseract-OCR");
//	   t.setLanguage("eng");

	   String result = t.doOCR(img);

	   System.out.println(result);
	   return result;

   }
	@PostMapping("/lockuser")
	 	public  ResponseEntity<Map<String,Object>> lockUser(@RequestBody ChatLock chatLock,@RequestHeader ("Authorization")String token) throws Exception
	 {
		 Map<String, Object>response =new HashMap<>();
		 if(userHandle.tokenIsValid(token))
		 {
             response=userHandle.setChatLock(chatLock);
			 return new ResponseEntity<>(response, HttpStatus.ACCEPTED
			 );

		 }

		 return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

	 }
	 @PostMapping("/verifychatlock")
	 public  ResponseEntity<Map<String,Object>> verifyChatLock(@RequestBody  ChatLock chatLock ,@RequestHeader ("Authorization")String token) throws ExecutionException, InterruptedException {
		 Map<String, Object>response =new HashMap<>();
		 if(userHandle.tokenIsValid(token))
		 {
			 response=userHandle.verifyChatLock(
					 chatLock);
			 return new ResponseEntity<>(response, HttpStatus.ACCEPTED
			 );

		 }
		 return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

	 }
	 @DeleteMapping("/removechatlock")
	 public  ResponseEntity<Map<String,Object>> removeChatLock(@RequestBody  ChatLock chatLock ,@RequestHeader ("Authorization")String token) throws ExecutionException, InterruptedException {
		 Map<String, Object>response =new HashMap<>();
		 if(userHandle.tokenIsValid(token))
		 {
			 response=userHandle.removeChatLock(

					 chatLock);
			 return new ResponseEntity<>(response, HttpStatus.ACCEPTED
			 );

		 }
		 return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

	 }


   
}
