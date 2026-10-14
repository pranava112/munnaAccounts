package com.vpm.Accounts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key:}")
    private String openAIKey;

   @Bean 
   public RestClient openAIRestClient(){
     return RestClient.builder()
           .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openAIKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
   }

    
}
