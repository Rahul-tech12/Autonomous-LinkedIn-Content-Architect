package com.example.project.Entity;
import com.example.project.Enums.PostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String content;

    private String tone;
    private String goal;
    private String targetAudience;

    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int copyCount;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isPublished = false;

    // ✅ NEW scheduling fields
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.DRAFT;

}

