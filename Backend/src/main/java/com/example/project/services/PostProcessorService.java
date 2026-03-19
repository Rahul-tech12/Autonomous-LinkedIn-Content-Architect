package com.example.project.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostProcessorService {

    private final GeminiService geminiService;

    public String processPost(String rawPost) {
        String prompt = """
            You are a LinkedIn post editor. Fix and enhance this post:

            TASK 1 — Fix spacing errors:
            - Fix all merged words (e.g. "justto" → "just to", "Hereis" → "Here is")
            - Fix missing spaces after punctuation
            - Fix missing spaces between sentences
            - Do NOT change any words or meaning

            TASK 2 — Add emojis strategically:
            - Add 1 relevant emoji at the end of the opening hook line
            - Add 1 emoji before each numbered point if present
            - Add 🎯 or 💡 before the key insight line
            - Add 💬👇 before the closing question/CTA
            - Never add more than 1 emoji per line
            - Never add emojis inside the middle of sentences
            - Keep hashtags at the end, no emojis in hashtags

            TASK 3 — Fix formatting:
            - Ensure each paragraph is separated by a blank line
            - Ensure numbered lists have consistent spacing
            - Keep the original tone and voice completely intact

            Return ONLY the improved post. No explanations. No preamble.

            POST TO IMPROVE:
            %s
            """.formatted(rawPost);

        return geminiService.generateLinkedInPost(prompt);
    }
}