package io.mountblue.dao;

import io.mountblue.model.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("SELECT p FROM Post p ORDER BY p.publishedAt DESC")
    List<Post> findAllByOrderByPublishedAtDesc();

    @Query("SELECT p FROM Post p ORDER BY p.publishedAt ASC")
    List<Post> findAllByOrderByPublishedAtAsc();

    @Query(value = "SELECT p.* FROM post p " +
            "JOIN users u ON p.user_id = u.id " +
            "WHERE to_tsvector('english', p.title || ' ' || p.content) " +
            " @@ to_tsquery('english', replace(:query, ' ', ' & ')) " +
            "OR EXISTS (SELECT 1 FROM post_tags pt " +
            "JOIN tags t ON pt.tag_id = t.id " +
            "WHERE pt.post_id = p.id " +
            "AND to_tsvector('english', t.name) @@ to_tsquery('english', replace(:query, ' ', ' & '))) " +
            "OR to_tsvector('english', u.name) @@ to_tsquery('english', replace(:query, ' ', ' & '))",
            nativeQuery = true)
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE (:authorNames IS NULL OR p.user.name IN :authorNames) " +
            "AND (p.createdAt BETWEEN :startDate AND :endDate) " +
            "AND (:tagNames IS NULL OR EXISTS (SELECT t FROM p.tags t WHERE t.name IN :tagNames))")
    Page<Post> filterPosts(@Param("authorNames") List<String> authorNames,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate,
                           @Param("tagNames") List<String> tagNames,
                           Pageable pageable);
}
