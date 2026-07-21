package com.ChatApp.Services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private FirebaseConfig fb;
   private  String emailTo;
    private int otp;
    LocalDateTime start;
    SecureRandom random = new SecureRandom();
    private boolean otpSave;
    private HashMap<Integer, String> saveEmailAndOtp = new HashMap<>();
    public boolean forgotPassword;

    public String sendEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vishalevoke27@gmail.com");
        message.setTo(to);
        start = LocalDateTime.now();
        if(forgotPassword)
        { 
        	if(fb.checkEmail(to))
        	{
        		message.setSubject("Forgot Password");
            	this.otp= random.nextInt(10000 - 1000 + 1) + 1000;
                System.out.println("send Email to "+to + " OTP = "+ otp);
                this.emailTo=to;
                this.otpSave= fb.otpSaveIntoDb(to, this.otp, start);
                saveEmailAndOtp.put(this.otp,this.emailTo);
                message.setText("Your Otp is "+String.valueOf(otp)   +" It will expire within 5 minute.");
               mailSender.send(message);
               this.forgotPassword=false;
               return "";
        	}
        	else
        	{
        		this.forgotPassword=false;
        		return "Email id does not exits";
        	}
        }
        else
        {
        	if(!fb.checkEmail(to))
        			{
        message.setSubject("Verification Code");
        this.otp= random.nextInt(10000 - 1000 + 1) + 1000;
        System.out.println("send Email to "+to + " OTP = "+ otp);
        this.emailTo=to;
        this.otpSave= fb.otpSaveIntoDb(to, this.otp,start);
        saveEmailAndOtp.put(this.otp,this.emailTo);
        message.setText("Your Otp is "+String.valueOf(otp)   +" It will expire within 5 minute");
       mailSender.send(message);
       return "";
        			}
        	else
        	{
        		System.out.println("Email id already exits");
        		return "Email id already exits";
        	}
        }
    }
    public boolean verifyOtp(int otpFromUser) throws Exception
    {
    	LocalDateTime end=LocalDateTime.now();
    	        
    	if (Duration.between(start, end).toMinutes() < 5)
        {
        
        	ArrayList<String>tem=fb.getUserEmail(otpFromUser);
        	if(!tem.isEmpty())
        	{
        	String temEmail=saveEmailAndOtp.get(otpFromUser);
        	if(String.valueOf(otpFromUser).equalsIgnoreCase(tem.get(1)) && tem.get(0).equalsIgnoreCase(temEmail))
        	{
                 
        	    fb.saveCredentialsIntoDb(temEmail);	
        	    fb.deleteOtpAndEmail(otpFromUser);
        		return true;
        	}
        	else
        	{
        		fb.deleteRecordFromUserCollection(saveEmailAndOtp.get(otpFromUser));
        		fb.deleteOtpAndEmail(otpFromUser);
        		return false;	
        	}
        	}
        	else
        	{
         	   fb.deleteRecordFromUserCollection(saveEmailAndOtp.get(otpFromUser));
          		fb.deleteOtpAndEmail(otpFromUser);
          		return false;
        	}
        }
    	else
    	{       
    		fb.deleteRecordFromUserCollection(saveEmailAndOtp.get(otpFromUser));
    		fb.deleteOtpAndEmail(otpFromUser);
    		return false;
    	}
    }
    
    public boolean verifyOtpPasswordChange(int otpFromUser) throws Exception
    {
    	LocalDateTime end=LocalDateTime.now();
    	        
    	if (Duration.between(start, end).toMinutes() < 5)
        {
        
        	ArrayList<String>tem=fb.getUserEmail(otpFromUser);
        	if(!tem.isEmpty())
        	{
        	String temEmail=saveEmailAndOtp.get(this.otp);
        	if(String.valueOf(otpFromUser).equalsIgnoreCase(tem.get(1)) && tem.get(0).equalsIgnoreCase(temEmail))
        	{
                 
        	    fb.deleteOtpAndEmail(otpFromUser);
        		return true;
        	}
        	else {
            	return false;

        	}
        	}
        	else
        	{
        		return false;
        	}
        }
        else
        {
    	    fb.deleteOtpAndEmail(otpFromUser);

        	return false;
        }
    }
    
    public boolean verifyOtpUpdate(int otp) throws InterruptedException, ExecutionException
    {
    
    	LocalDateTime end=LocalDateTime.now();
        
    	if (Duration.between(start, end).toMinutes() < 5)
        {
        
        	ArrayList<String>tem=fb.getUserEmail(otp);
        	if(!tem.isEmpty())
        	{
        	String temEmail=saveEmailAndOtp.get(this.otp);
        	if(String.valueOf(otp).equalsIgnoreCase(tem.get(1)) && tem.get(0).equalsIgnoreCase(temEmail))
        	{
                 
        	    fb.deleteOtpAndEmail(otp);
        		return true;
        	}
        	else {
            	return false;

        	}
        	}
        	else
        	{
        		return false;
        	}
        }
        else
        {
    	    fb.deleteOtpAndEmail(otp);

        	return false;
        }
    }
    @Scheduled(cron = "0 0 */3 * * *")    
    public void deleteOtp()
    {
    	fb.deleteOtp();
    }
//    @Scheduled(cron = "* * * * * *")
//    public void sendEmail()
//    {
//    	  SimpleMailMessage message = new SimpleMailMessage();
//          message.setFrom("vishalevoke27@gmail.com");
//          message.setTo("vk368065@gmail.com");
//          message.setSubject("I can’t stop thinking… did I lose you somewhere?");
//          message.setText("Hey,\n\nI’ve been trying to ignore it, but I couldn’t. When I noticed I’m no longer on your Instagram list, something inside me just felt off.\n\nIt’s not just about the list… it’s about what it might mean. I keep replaying things in my head, wondering if I said or did something that pushed you away.\n\nIf I did, I truly didn’t mean to. You matter to me more than I probably ever said out loud, and that’s why this is bothering me so much.\n\nMaybe it’s nothing, maybe I’m just overthinking… but I needed to ask, because silence feels heavier than the truth.\n\nI hope you’re okay. And I hope you’ll tell me what really happened.");
//        mailSender.send(message);
//
//  		
//
//
//          
//    }
    
    
}
