package com.example.project.Repository;

import com.example.project.Entity.PostEntity;
import com.example.project.Enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository  extends JpaRepository<PostEntity,Long> {

    List<PostEntity> findAllByOrderByCreatedAtDesc();

    List<PostEntity> findByStatusAndScheduledAtBefore(
            PostStatus status,
            LocalDateTime dateTime
    );

    List<PostEntity> findByStatus(PostStatus postStatus);
}
