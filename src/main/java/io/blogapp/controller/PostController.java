package io.blogapp.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public String fetchAllPosts(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size, Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getAllPosts(pageable);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("currentPage", page);

        System.out.println(SecurityContextHolder.getContext().getAuthentication());
        return "posts/list";
    }

    @GetMapping("/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/postcreate";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post, @RequestParam String tag) {
        postService.createPost(post, tag);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String getPostDetails(@PathVariable UUID id, Model model) {

        Post post = postService.getPostById(id);
        if (post == null) {
            throw new ResourceNotFoundException("Post with ID " + id + " not found.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = userService.isAdmin();
        List<Comment> comments = commentService.getAllComment(id);
        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUsername);
        model.addAttribute("comment", comments);
        model.addAttribute("isAdmin", isAdmin);
        return "posts/detailsofpost";
    }

    @DeleteMapping("/delete/{id}")
    public String deletePost(@PathVariable UUID id) {
        postService.delete(id);
        return "redirect:/";
    }

    @GetMapping("/filter")
    public String filter(@RequestParam(required = false) String author,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(required = false) String tag,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postService.filterPosts(author, startDate, endDate, tag, pageable);

        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        return "posts/list";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam("query") String query,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size, Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.searchPosts(query, pageable);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("query", query);
        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("currentPage", page);
        return "posts/list";
    }

    @GetMapping("/update/{id}")
    public String updatePost(@PathVariable UUID id, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);

        String tagsString = post.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.joining(", "));
        model.addAttribute("tagsInput", tagsString);

        return "posts/updatepost";
    }

    @PostMapping("/update")
    public String updatePost(@RequestParam("id") String id,
                             @RequestParam("tagsInput") String tagsInput,
                             @RequestParam("title") String title,
                             @RequestParam("excerpt") String excerpt,
                             @RequestParam("content") String content, Model model) {
        UUID uuid = UUID.fromString(id);
        Post post = postService.getPostById(uuid);
        if (post == null) {
            throw new ResourceNotFoundException("Post with ID " + id + " not found.");
        }
        postService.updatePost(uuid, title, excerpt, content);
        postService.updatePostTags(uuid, tagsInput);
        boolean isAdmin = userService.isAdmin();
        model.addAttribute("post", post);
        model.addAttribute("isAdmin", isAdmin);
        return "posts/detailsofpost";
    }

    @GetMapping("/sort")
    public String sortPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            Model model) {
        Page<Post> sortedPosts = postService.sortPosts(page, size, sortBy, ascending);
        model.addAttribute("posts", sortedPosts.getContent());
        model.addAttribute("currentPage", sortedPosts.getNumber());
        model.addAttribute("totalPages", sortedPosts.getTotalPages());
        model.addAttribute("pageSize", sortedPosts.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("ascending", ascending);
        return "posts/list";
    }
}