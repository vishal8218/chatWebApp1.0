package com.ChatApp.Services;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.ChatApp.Models.UserId;

import org.springframework.messaging.Message;


@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil; // your JWT utility
    
    @Autowired
	FirebaseConfiguration fbs;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = 
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");


            String userId = jwtUtil.extractUsername(token); // your JWT decode method
            HashMap<String,Object>senderId=(HashMap<String, Object>) fbs.getSenderId(userId);
            accessor.setUser(new UserId(senderId.get("UserId").toString(),senderId.get("UserId").toString()));
        }

        return message;
    }
}
