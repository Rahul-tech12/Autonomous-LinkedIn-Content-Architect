package com.example.project.services;

import com.example.project.Entity.PostEntity;
import com.example.project.Enums.PostStatus;
import com.example.project.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final GeminiService geminiService;
    private final PostProcessorService postProcessorService;
    private final LinkedInService linkedInService;
    private final PostRepository postRepository;

    // ✅ Runs every day at 9am
    @Scheduled(cron = "0 28 18 * * SUN,THU")
    public void runDailyPostingAgent() {
        log.info("🤖 Agent started");

        try {
            // Step 1 — Agent decides what to post about
            String topicDecision = geminiService.generateLinkedInPost("""
                You are a LinkedIn content strategist for a software engineer.
                
                Today's date: %s
                
                Suggest ONE trending and relevant topic to post about today
                in the software engineering / AI / career space.
                
                Consider:
                - What's trending in tech this week
                - Topics that drive high engagement
                - Variety (don't repeat same topic category daily)
                
                Return ONLY the topic in one sentence. Nothing else.
                """.formatted(java.time.LocalDate.now()));

            log.info("🤖 Agent chose topic: {}", topicDecision);

            // Step 2 — Agent generates the post
            String rawPost = geminiService.generateLinkedInPost("""
                Write a LinkedIn post about: %s
                
                Tone: Thought leadership
                Audience: Software engineers and tech students
                Length: 200-300 words
                Goal: Maximum engagement
                
                Structure:
                1. Hook — scroll-stopping first line
                2. Body — insight, story, or data
                3. CTA — question that drives comments
                
                Add 3-5 relevant hashtags.
                """.formatted(topicDecision));

            // Step 3 — Agent polishes the post
            String polishedPost = postProcessorService.processPost(rawPost);

            // Step 4 — Agent posts to LinkedIn
            linkedInService.publishPost(polishedPost);

            // Step 5 — Agent saves record to DB
            PostEntity post = new PostEntity();
            post.setContent(polishedPost);
            post.setTone("Thought Leadership");
            post.setGoal("Engagement");
            post.setCreatedAt(java.time.LocalDateTime.now());
            post.setStatus(PostStatus.POSTED);
            postRepository.save(post);

            log.info("✅ Agent completed. Post published to LinkedIn.");

        } catch (Exception e) {
            log.error("🤖 Agent failed: {}", e.getMessage());
        }
    }
}
