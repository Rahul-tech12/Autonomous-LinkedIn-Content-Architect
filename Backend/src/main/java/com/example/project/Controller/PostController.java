package com.example.project.Controller;

import com.example.project.Dto.GeneratePostRequestDto;
import com.example.project.Entity.PostEntity;
import com.example.project.Repository.PostRepository;
import com.example.project.services.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    public final PostService postService;
    public final PostRepository postRepository;

    // ✅ Streaming endpoint — does NOT save to DB
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generatePostStream(@RequestBody GeneratePostRequestDto request) {
        return postService.generatePostStream(request);
    }

    // ✅ Save endpoint — called from frontend after stream completes
    @PostMapping("/save")
    public ResponseEntity<PostEntity> savePost(@RequestBody Map<String, String> body) {
        PostEntity saved = postService.processAndSave(
                body.get("content"),
                body.get("tone"),
                body.get("goal"),
                body.get("targetAudience")
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PostEntity>> getHistory() {
        return ResponseEntity.ok(postService.getPostHistory());
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> markPublished(@PathVariable Long id) {
        postService.markAsPublished(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<PostEntity> schedulePost(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LocalDateTime scheduledAt = LocalDateTime.parse(body.get("scheduledAt"));
        PostEntity post = postService.schedulePost(id, scheduledAt);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<PostEntity>> getScheduled() {
        return ResponseEntity.ok(postService.getScheduledPosts());
    }

    @DeleteMapping("/{id}/schedule")
    public ResponseEntity<Void> cancelSchedule(@PathVariable Long id) {
        postService.cancelSchedule(id);
        return ResponseEntity.ok().build();
    }
}