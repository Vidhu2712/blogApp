package io.blogapp.restapicontroller;

import io.blogapp.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentRestController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<String> addComment(@RequestParam String message, @RequestParam String postId) {
        UUID uuid = UUID.fromString(postId);
        commentService.addComment(message, uuid);
        return ResponseEntity.status(201).body("Comment added successfully to post ID: " + postId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateComment(
            @PathVariable("id") UUID commentId,
            @RequestParam String message) {
        commentService.updateComment(commentId, message);
        return ResponseEntity.ok("Comment updated successfully for comment ID: " + commentId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable("id") UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully for comment ID: " + commentId);
    }
}
