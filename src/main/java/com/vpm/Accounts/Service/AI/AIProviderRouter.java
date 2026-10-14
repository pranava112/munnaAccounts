
package com.vpm.Accounts.Service.AI;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vpm.Accounts.DTO.AI.AIAccountDTO;
import com.vpm.Accounts.DTO.AI.AIProductDTO;
import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;

@Service
public class AIProviderRouter {

    private final OllamaAIService ollamaAIService;
    private final OpenAIService openAIService;
    private final String provider;

    public AIProviderRouter(
            OllamaAIService ollamaAIService,
            OpenAIService openAIService,
            @Value("${ai.provider:ollama}") String provider) {

        this.ollamaAIService = ollamaAIService;
        this.openAIService = openAIService;
        this.provider = provider;
    }

    public AITransactionDraftDTO scanDocument(
            MultipartFile file,
            List<AIAccountDTO> accounts,
            List<AIProductDTO> products) throws Exception {

        if ("ollama".equalsIgnoreCase(provider)) {
            return ollamaAIService.scanDocument(file, accounts, products);
        }

        if ("openai".equalsIgnoreCase(provider)) {
            return openAIService.scanDocument(file, accounts, products);
        }

        throw new IllegalStateException(
                "Unsupported AI provider: " + provider
                + ". Choose ollama or openai.");
    }
}
