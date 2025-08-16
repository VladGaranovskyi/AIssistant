package com.aissistant.demo.services.LLMService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.google.genai.Client;
import org.springframework.context.annotation.Bean;

@Configuration
public class LLMConfig {
    @Value("${aissistant.app.GOOGLE_AI_API_KEY}")
    private String jwtSecret;
    @Bean
    public Client getClient() {
        Client client = Client.builder().apiKey(jwtSecret).build();
        return client;
    }
}