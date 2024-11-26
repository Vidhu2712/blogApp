package io.mountblue.service;

import io.mountblue.dao.PostRepository;
import io.mountblue.dao.TagRepository;
import io.mountblue.model.Post;
import io.mountblue.model.Tags;
import io.mountblue.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagRepository tagRepository;

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id).orElse(null);
    }

    public Page<Post> getFilteredPosts(User user, String tags, LocalDateTime startDate,
                                       LocalDateTime endDate,Pageable pageable) {
        String author = (user != null) ? user.getName() : null;
        System.out.println(tags);


        // If no filters are provided, return all posts
        if (author == null && (tags == null || tags.isEmpty()) && startDate == null && endDate == null) {
            return postRepository.findAll(pageable);
        }

        // Filter by user only
        if (author != null && (tags == null || tags.isEmpty()) && startDate == null && endDate == null) {
            return postRepository.findFilteredPostsByUser(author, pageable);
        }

        // Filter by date only
        if (author == null && (tags == null || tags.isEmpty())) {
            return postRepository.filterByDate(startDate, endDate, pageable);
        }

        // Filter by tags only
        if (tags != null && !tags.isEmpty() && author == null && startDate == null && endDate == null) {
            return postRepository.findFilteredPostsByTags(tags, pageable);
        }

        // Filter by author and tags
        if (author != null && tags != null && !tags.isEmpty()) {
            return postRepository.findByUserAndTagsAndDate(author, tags, startDate, endDate, pageable);
        }

        // Filter by author and date range
        if (author != null && startDate != null && endDate != null) {
            return postRepository.findFilteredPostsByUserAndDate(author, startDate, endDate, pageable);
        }

        // Filter by tags and date range
        if (tags != null && !tags.isEmpty() && startDate != null && endDate != null) {
            List<String> tagList = tags != null ? Arrays.asList(tags.split(",")) : null;

            // Call the repository method
            return postRepository.findFilteredPostsByTagsAndDate(tagList, startDate, endDate, pageable);
//            return postRepository.findFilteredPostsByTagsAndDate(tags, startDate, endDate, pageable);
        }

        return postRepository.filterByDate(startDate, endDate, pageable);
    }

    public List<Post> getAllPostsSortedByPublishedDateDesc() {
        return postRepository.findAllByOrderByPublishedAtDesc();
    }

    public Object getAllPostsSortedByPublishedDateAsc() {
        return postRepository.findAllByOrderByPublishedAtAsc();
    }

    public Page<Post> searchPosts(String query,Pageable pageable) {
        return postRepository.searchPosts(query,pageable);
    }


    public void delete(UUID id) {
        postRepository.deleteById(id);
    }

    public void createPost(Post post, String tagsName) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(userDetails.getUsername());

        if ((user != null && user.getRole().equals("USER")) || (user!=null && user.getRole().equals("ADMIN"))) {
            post.setUser(user);
            post.setPublished(true);
        }

        if (tagsName != null && !tagsName.isEmpty()) {
            String[] tagNames = tagsName.split(",");
            Set<Tags> tagsSet = new HashSet<>();

            for (String tagName : tagNames) {
                tagName = tagName.trim();
                Tags tag = tagService.getOrCreateTag(tagName);
                tagsSet.add(tag);
            }
            post.setTags(tagsSet);
        }
        postRepository.save(post);
    }
    public void updatePostTags(UUID postId, String tagsInput) {
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByEmail(username);
        if ((user != null && user.getRole().equals("USER")) || (user!=null && user.getRole().equals("ADMIN"))) {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

            Set<String> tagNames = Stream.of(tagsInput.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());

            Set<Tags> updatedTags = new HashSet<>();
            for (String tagName : tagNames) {
                Tags tag = tagRepository.findByName(tagName);
                if(tag==null){
                    tag = new Tags();
                    tag.setName(tagName);
                    tagRepository.save(tag);
                }
                updatedTags.add(tag);
            }

            post.setTags(updatedTags);
            postRepository.save(post);
        }
    }

    public void updatePost(UUID uuid, String title, String excerpt, String content){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByEmail(username);
        if ((user != null && user.getRole().equals("USER")) || (user!=null && user.getRole().equals("ADMIN"))) {
            Optional<Post> optionalPost = postRepository.findById(uuid);
            if (optionalPost.isPresent()) {
                Post post = optionalPost.get();
                post.setTitle(title);
                post.setExcerpt(excerpt);
                post.setContent(content);
                postRepository.save(post);
            }
        }
    }

    public Page<Post> sortPosts(int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(Sort.Direction.ASC, sortBy) : Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return postRepository.findAll(pageable);
    }
}