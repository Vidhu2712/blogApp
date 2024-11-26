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

    public Page<Post> filterPosts(String authorNames, String startDate, String endDate,
                                  String tagNames, Pageable pageable) {

        List<String> authorNameList = (authorNames != null && !authorNames.trim().isEmpty())
                ? List.of(authorNames.split(",\\s*"))
                : null;

        List<String> tagNameList = (tagNames != null && !tagNames.trim().isEmpty())
                ? List.of(tagNames.split(",\\s*"))
                : null;

        LocalDateTime startDateTime = (startDate != null && !startDate.trim().isEmpty())
                ? LocalDateTime.parse(startDate + "T00:00:00")
                : LocalDateTime.of(2024, 1, 1, 0, 0);

        LocalDateTime endDateTime = (endDate != null && !endDate.trim().isEmpty())
                ? LocalDateTime.parse(endDate + "T23:59:59")
                : LocalDateTime.now();

        return postRepository.filterPosts(authorNameList, startDateTime, endDateTime, tagNameList, pageable);
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

        if ((user != null && user.getRole().equals("USER")) ||  (user!=null && user.getRole().equals("ADMIN"))) {
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
                if (tag == null) {
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
        if (user != null && user.getRole().equals("USER") || (user!=null && user.getRole().equals("ADMIN"))) {
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