package io.blogapp.restapicontroller;

import io.blogapp.exception.ResourceNotFoundException;
import io.blogapp.model.Comment;
import io.blogapp.model.Post;
import io.blogapp.service.CommentService;
import io.blogapp.service.PostService;
import io.blogapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostRestController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> fetchAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getAllPosts(pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts.getContent());
        response.put("totalPages", posts.getTotalPages());
        response.put("currentPage", page);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Map<String, Object> postRequest) {
        Post post = new Post(); // populate fields from request
        String tag = (String) postRequest.get("tag");
        postService.createPost(post, tag);
        return ResponseEntity.status(201).body(post);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPostDetails(@PathVariable UUID id) {
        Post post = postService.getPostById(id);
        if (post == null) {
            throw new ResourceNotFoundException("Post with ID " + id + " not found.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = userService.isAdmin();
        List<Comment> comments = commentService.getAllComment(id);

        Map<String, Object> response = new HashMap<>();
        response.put("post", post);
        response.put("currentUser", currentUsername);
        response.put("comments", comments);
        response.put("isAdmin", isAdmin);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterPosts(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postService.filterPosts(author, startDate, endDate, tag, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", postPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", postPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.searchPosts(query, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts.getContent());
        response.put("query", query);
        response.put("totalPages", posts.getTotalPages());
        response.put("currentPage", page);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updateRequest) {
        String tagsInput = (String) updateRequest.get("tagsInput");
        String title = (String) updateRequest.get("title");
        String excerpt = (String) updateRequest.get("excerpt");
        String content = (String) updateRequest.get("content");

        Post post = postService.getPostById(id);
        if (post == null) {
            throw new ResourceNotFoundException("Post with ID " + id + " not found.");
        }

        postService.updatePost(id, title, excerpt, content);
        postService.updatePostTags(id, tagsInput);

        return ResponseEntity.ok(post);
    }

    @GetMapping("/sort")
    public ResponseEntity<Map<String, Object>> sortPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {

        Page<Post> sortedPosts = postService.sortPosts(page, size, sortBy, ascending);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", sortedPosts.getContent());
        response.put("currentPage", sortedPosts.getNumber());
        response.put("totalPages", sortedPosts.getTotalPages());
        response.put("pageSize", sortedPosts.getSize());
        response.put("sortBy", sortBy);
        response.put("ascending", ascending);

        return ResponseEntity.ok(response);
    }
}

