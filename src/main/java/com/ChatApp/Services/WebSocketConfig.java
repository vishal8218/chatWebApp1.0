package com.ChatApp.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	 @Autowired
	    private JwtAuthChannelInterceptor jwtAuthChannelInterceptor;
	 
	 @Override
	    public void configureClientInboundChannel(ChannelRegistration registration) {
	        registration.interceptors(jwtAuthChannelInterceptor);
	    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue","/topic"); // use queue for private messages
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user"); // prefix for user-specific
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:3000").withSockJS();
    	//registry.addEndpoint("/ws");
    }
   
  

  

}

