package com.example.project.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class GeminiService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    private static final String SYSTEM_PROMPT = """
            You are an expert LinkedIn content strategist with 10+ years of experience
            helping professionals grow their personal brand. You write posts that feel
            authentic, drive real engagement, and spark meaningful conversations.
            Always write in first person. Never use generic buzzwords.
            Format the post with line breaks for readability.
            Add 3-5 relevant hashtags at the end.
            """;

    public GeminiService(ChatClient.Builder builder, ChatModel chatModel) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
        this.chatModel = chatModel;
    }

    // ✅ Original — used for saving to DB
    public String generateLinkedInPost(String promptText) {
        try {
            return chatClient
                    .prompt()
                    .user(promptText)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Gemini generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate LinkedIn post", e);
        }
    }

    // ✅ TRUE native streaming from Gemini
    public Flux<String> streamLinkedInPost(String promptText) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(promptText)
        ));

        return chatModel.stream(prompt)
                .mapNotNull(response -> {
                    try {
                        String text = response.getResult()
                                .getOutput()
                                .getText();
                        if (text != null && !text.isEmpty()) {
                            // ✅ Encode newlines so SSE protocol doesn't break
                            return text.replace("\n", "<<N>>");
                        }
                        return null;
                    } catch (Exception e) {
                        return null;
                    }
                });
    }
}