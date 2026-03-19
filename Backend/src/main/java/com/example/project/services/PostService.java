package com.example.project.services;

import com.example.project.Dto.GeneratePostRequestDto;
import com.example.project.Entity.PostEntity;
import com.example.project.Enums.PostStatus;
import com.example.project.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    public final PostRepository postRepository;

    private final GeminiService geminiService;
    private final PostProcessorService postProcessorService;

   public PostEntity generateAndSave(GeneratePostRequestDto request) {
    String prompt = buildPrompt(request);
    String content = geminiService.generateLinkedInPost(prompt);

    PostEntity post = new PostEntity();
    post.setTitle(request.getTopic());
    post.setContent(content);
    post.setTone(request.getTone());
    post.setGoal(request.getGoal());
    post.setTargetAudience(request.getTargetAudience());
    post.setCreatedAt(LocalDateTime.now());
    post.setStatus(PostStatus.DRAFT);

    return postRepository.save(post);
}

    public Flux<String> generatePostStream(GeneratePostRequestDto request) {
        String prompt = buildPrompt(request);
        return geminiService.streamLinkedInPost(prompt);
    }

    // ✅ Called from /save endpoint — processes before saving
    public PostEntity processAndSave(String content, String tone,
                                     String goal, String targetAudience) {
        log.info("Processing post before saving...");

        // ✅ Fix spacing + add emojis automatically
        String processedContent = postProcessorService.processPost(content);

        PostEntity post = new PostEntity();
        post.setContent(processedContent);
        post.setTone(tone);
        post.setGoal(goal);
        post.setTargetAudience(targetAudience);
        post.setCreatedAt(LocalDateTime.now());
        post.setStatus(PostStatus.DRAFT);

        return postRepository.save(post);
    }

    public List<PostEntity> getPostHistory(){
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    private String buildPrompt(GeneratePostRequestDto req) {
        String lengthInstruction = switch (req.getLength()) {
            case "Short" -> "Keep it under 100 words. Punchy and direct.";
            case "Long" -> "Write 300-400 words. Go deep with storytelling or data.";
            default -> "Write 120-200 words. Balanced and informative.";
        };

        String goalInstruction = switch (req.getGoal()) {
            case "Authority" -> "Position the author as a thought leader. Use data, insights, and confident opinions.";
            case "Leads" -> "Subtly mention a service/product and end with a soft CTA to DM or comment.";
            case "Hiring" -> "Highlight team culture and the opportunity. Make it feel human, not like a job ad.";
            default -> "Maximise comments and shares. Ask a thought-provoking question at the end.";
        };

        return """
            Write a LinkedIn post about: %s
            
            Target audience: %s
            Tone: %s
            
            Length instruction: %s
            Goal instruction: %s
            
            Structure:
            1. Hook — first line must stop the scroll
            2. Body — deliver value, story, or insight
            3. CTA — end with a question or clear next step
            """.formatted(
                req.getTopic(),
                req.getTargetAudience(),
                req.getTone(),
                lengthInstruction,
                goalInstruction
        );
    }

    public void markAsPublished(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        post.setPublished(true);
        postRepository.save(post);
    }

    public PostEntity schedulePost(Long id, LocalDateTime scheduledAt) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setScheduledAt(scheduledAt);
        post.setStatus(PostStatus.SCHEDULED);
        return postRepository.save(post);
    }

    public List<PostEntity> getScheduledPosts() {
        return postRepository.findByStatus(PostStatus.SCHEDULED);
    }

    public void cancelSchedule(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.DRAFT);
        post.setScheduledAt(null);
        postRepository.save(post);
    }
}

