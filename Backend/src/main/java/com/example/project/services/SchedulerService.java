package com.example.project.services;

import com.example.project.Entity.PostEntity;
import com.example.project.Enums.PostStatus;
import com.example.project.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final PostRepository postRepository;
    private final LinkedInService linkedInService;

    // ✅ Runs every 30 seconds — checks for due posts
    @Scheduled(fixedDelay = 30000)
    public void processScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();

        List<PostEntity> duePosts = postRepository
                .findByStatusAndScheduledAtBefore(
                        PostStatus.SCHEDULED, now
                );

        if (duePosts.isEmpty()) return;

        log.info("Found {} posts due for publishing", duePosts.size());

        for (PostEntity post : duePosts) {
            try {
                log.info("Posting to LinkedIn: postId={}", post.getId());
                linkedInService.publishPost(post.getContent());

                post.setStatus(PostStatus.POSTED);
                post.setPublished(true);
                postRepository.save(post);

                log.info("Successfully posted: postId={}", post.getId());

            } catch (Exception e) {
                log.error("Failed to post: postId={}, error={}",
                        post.getId(), e.getMessage());
                post.setStatus(PostStatus.FAILED);
                postRepository.save(post);
            }
        }
    }
}
