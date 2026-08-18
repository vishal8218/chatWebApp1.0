package com.ChatApp.Services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final long OTP_EXPIRY_MINUTES = 5;
	private static final String FROM_ADDRESS = "vishalevoke27@gmail.com";

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private FirebaseConfig fb;
	private String emailTo;
	private int otp;
	LocalDateTime start;
	SecureRandom random = new SecureRandom();
	private boolean otpSave;
	private HashMap<Integer, String> saveEmailAndOtp = new HashMap<>();
	public boolean forgotPassword;

	public String sendEmail(String to) {
		start = LocalDateTime.now();
		if (forgotPassword) {
			if (fb.checkEmail(to)) {
				this.otp = random.nextInt(10000 - 1000 + 1) + 1000;
				this.emailTo = to;
				this.otpSave = fb.otpSaveIntoDb(to, this.otp, start);
				saveEmailAndOtp.put(this.otp, this.emailTo);
				sendOtpEmail(to, "Forgot Password", this.otp);
				this.forgotPassword = false;
				return "";
			} else {
				this.forgotPassword = false;
				return "Email id does not exits";
			}
		} else {
			if (!fb.checkEmail(to)) {
				this.otp = random.nextInt(10000 - 1000 + 1) + 1000;
				this.emailTo = to;
				this.otpSave = fb.otpSaveIntoDb(to, this.otp, start);
				saveEmailAndOtp.put(this.otp, this.emailTo);
				sendOtpEmail(to, "Verification Code", this.otp);
				return "";
			} else {
				return "Email id already exits";
			}
		}
	}

	/**
	 * Sends the OTP as a modern HTML email instead of the old plain-text message.
	 * OTP is no longer printed to console (avoids leaking secrets to logs).
	 */
	private void sendOtpEmail(String to, String subject, int otp) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			helper.setFrom(FROM_ADDRESS);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(buildOtpEmailHtml(subject, otp), true);
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send OTP email to " + to, e);
		}
	}

	/**
	 * Builds a modern, card-style HTML email. Styles are inlined since most
	 * email clients (Gmail, Outlook) strip <style> blocks.
	 */
	private String buildOtpEmailHtml(String heading, int otp) {
		String otpDigits = String.valueOf(otp);
		StringBuilder digitBoxes = new StringBuilder();
		for (char c : otpDigits.toCharArray()) {
			digitBoxes.append(
					"<td style=\"width:44px;height:56px;background:#f4f5ff;border:1px solid #e2e4fa;" +
							"border-radius:10px;text-align:center;vertical-align:middle;" +
							"font-family:'Segoe UI',Arial,sans-serif;font-size:26px;font-weight:700;" +
							"color:#3b3fd9;\">" + c + "</td>" +
							"<td style=\"width:8px;\"></td>"
			);
		}

		return "<!DOCTYPE html>" +
				"<html><body style=\"margin:0;padding:0;background:#eef0fb;" +
				"font-family:'Segoe UI',Arial,sans-serif;\">" +
				"<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" " +
				"style=\"background:#eef0fb;padding:32px 0;\"><tr><td align=\"center\">" +

				"<table role=\"presentation\" width=\"420\" cellpadding=\"0\" cellspacing=\"0\" " +
				"style=\"background:#ffffff;border-radius:16px;overflow:hidden;" +
				"box-shadow:0 8px 24px rgba(59,63,217,0.12);\">" +

				// Header
				"<tr><td style=\"background:linear-gradient(135deg,#6366f1,#3b82f6);" +
				"padding:28px 32px;text-align:center;\">" +
				"<div style=\"font-size:20px;font-weight:700;color:#ffffff;letter-spacing:0.3px;\">" +
				"ChatApp</div>" +
				"<div style=\"font-size:13px;color:#e0e7ff;margin-top:4px;\">" + heading + "</div>" +
				"</td></tr>" +

				// Body
				"<tr><td style=\"padding:32px 32px 8px 32px;text-align:center;\">" +
				"<p style=\"margin:0 0 20px 0;font-size:15px;color:#4b4f6b;line-height:1.5;\">" +
				"Use the verification code below to continue. This code is valid for " +
				"<strong>" + OTP_EXPIRY_MINUTES + " minutes</strong>.</p>" +

				"<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">" +
				"<tr>" + digitBoxes + "</tr></table>" +

				"<p style=\"margin:24px 0 0 0;font-size:13px;color:#9497b3;line-height:1.5;\">" +
				"Didn't request this code? You can safely ignore this email.</p>" +
				"</td></tr>" +

				// Footer
				"<tr><td style=\"padding:24px 32px 28px 32px;text-align:center;" +
				"border-top:1px solid #f0f1f9;margin-top:16px;\">" +
				"<p style=\"margin:16px 0 0 0;font-size:11px;color:#c2c4d6;\">" +
				"&copy; " + LocalDateTime.now().getYear() + " ChatApp. All rights reserved.</p>" +
				"</td></tr>" +

				"</table>" +
				"</td></tr></table>" +
				"</body></html>";
	}

	public boolean verifyOtp(int otpFromUser) throws Exception {
		LocalDateTime end = LocalDateTime.now();

		if (Duration.between(start, end).toMinutes() < 5) {

			ArrayList<String> tem = fb.getUserEmail(otpFromUser);
			if (!tem.isEmpty()) {
				String temEmail = saveEmailAndOtp.get(otpFromUser);
				if (String.valueOf(otpFromUser).equalsIgnoreCase(tem.get(1)) && tem.get(0).equalsIgnoreCase(temEmail)) {

					fb.saveCredentialsIntoDb(temEmail);
					fb.deleteOtpAndEmail(otpFromUser);
					return true;
				} else {
					fb.deleteRecordFromUserCollection(saveEmailAndOtp.get(otpFromUser));
					fb.deleteOtpAndEmail(otpFromUser);
					return false;
				}
			} else {
				fb.deleteRecordFromUserCollection(saveEmailAndOtp.get(otpFromUser));
				fb.deleteOtpAndEmail(otpFromUser);
				return false;
			}
		} else {
			fb.deleteRecordFromUserCollection(saveEmailAndOtp.get(otpFromUser));
			fb.deleteOtpAndEmail(otpFromUser);
			return false;
		}
	}

	public boolean verifyOtpPasswordChange(int otpFromUser) throws Exception {
		LocalDateTime end = LocalDateTime.now();

		if (Duration.between(start, end).toMinutes() < 5) {

			ArrayList<String> tem = fb.getUserEmail(otpFromUser);
			if (!tem.isEmpty()) {
				String temEmail = saveEmailAndOtp.get(this.otp);
				if (String.valueOf(otpFromUser).equalsIgnoreCase(tem.get(1)) && tem.get(0).equalsIgnoreCase(temEmail)) {

					fb.deleteOtpAndEmail(otpFromUser);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			fb.deleteOtpAndEmail(otpFromUser);

			return false;
		}
	}

	public boolean verifyOtpUpdate(int otp) throws InterruptedException, ExecutionException {

		LocalDateTime end = LocalDateTime.now();

		if (Duration.between(start, end).toMinutes() < 5) {

			ArrayList<String> tem = fb.getUserEmail(otp);
			if (!tem.isEmpty()) {
				String temEmail = saveEmailAndOtp.get(this.otp);
				if (String.valueOf(otp).equalsIgnoreCase(tem.get(1)) && tem.get(0).equalsIgnoreCase(temEmail)) {

					fb.deleteOtpAndEmail(otp);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			fb.deleteOtpAndEmail(otp);

			return false;
		}
	}

@Scheduled(cron = "0 */5 * * * *")
public void deleteOtp() {
    fb.deleteOtp();
}

}
