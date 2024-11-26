package io.mountblue.controller;

import io.mountblue.dao.UserRepository;
import io.mountblue.model.Comment;
import io.mountblue.model.Post;
import io.mountblue.model.Tags;
import io.mountblue.model.User;
import io.mountblue.service.CommentService;
import io.mountblue.service.PostService;
import io.mountblue.service.TagService;
import io.mountblue.service.UserService;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.Fetch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @GetMapping
    public String getAllPosts(@RequestParam(defaultValue = "0") int page,
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
        return "posts/create-post";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post, @RequestParam String tag) {
        postService.createPost(post, tag);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String getPostDetails(@PathVariable UUID id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Post post = postService.getPostById(id);
        List<Comment> comments = commentService.getAllComment(id);
        boolean isAdmin = userService.isAdmin();
        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUsername);
        model.addAttribute("comment", comments);
        model.addAttribute("isAdmin",isAdmin);
        return "posts/post-details";
    }

    @DeleteMapping("/delete/{id}")
    public String deletePost(@PathVariable UUID id) {
        postService.delete(id);
        return "redirect:/";
    }

    @GetMapping("/filter")
    public String filterPosts(@RequestParam(required = false) String author,
                              @RequestParam(required = false) String tag,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atStartOfDay().plusDays(1).minusNanos(1) : null; // Include full end date
        User user = null;
        if (author != null && !author.isEmpty()) {
            user = userService.findByUsername(author);
            if (user == null) {
                model.addAttribute("error", "No user found with the username: " + author);
                return "posts/list";
            }
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> filteredPosts = postService.getFilteredPosts(user, tag, startDateTime, endDateTime, pageable);
        model.addAttribute("posts", filteredPosts.getContent());
        model.addAttribute("totalPages", filteredPosts.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("author", author);
        model.addAttribute("tag", tag);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "posts/list";
    }


    @GetMapping("/posts")
    public String getAllPosts(@RequestParam(defaultValue = "asc") String sort, Model model) {
        if ("desc".equalsIgnoreCase(sort)) {
            model.addAttribute("posts", postService.getAllPostsSortedByPublishedDateDesc());
        } else {
            model.addAttribute("posts", postService.getAllPostsSortedByPublishedDateAsc());
        }
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
    public String updatePostForm(@PathVariable UUID id, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);

        String tagsString = post.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.joining(", "));
        model.addAttribute("tagsInput", tagsString);

        return "posts/post-update";
    }

    @PostMapping("/update")
    public String updatePost(@RequestParam("id") String id,
                             @RequestParam("tagsInput") String tagsInput,
                             @RequestParam("title") String title,
                             @RequestParam("excerpt") String excerpt,
                             @RequestParam("content") String content, Model model) {
        UUID uuid = UUID.fromString(id);
//        String currentUserEmail = principal.getName();
//        boolean isAdmin = userService.isAdmin(currentUserEmail);
        boolean isAdmin = userService.isAdmin(); // Check if the current user is an admin
//        String currentUserEmail = userService.getCurrentUserEmail(); // Get the logged-in user's email

        Post post = postService.getPostById(uuid);
        postService.updatePost(uuid, title, excerpt, content);
        postService.updatePostTags(uuid, tagsInput);
        model.addAttribute("post", post);
        model.addAttribute("isAdmin", isAdmin);
        return "posts/post-details";
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
